package ru.yandex.practicum.analyzer.snapshot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.event.model.Action;
import ru.yandex.practicum.analyzer.event.model.Condition;
import ru.yandex.practicum.analyzer.event.model.Scenario;
import ru.yandex.practicum.analyzer.event.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.snapshot.service.handler.ConditionHandlerFactory;
import ru.yandex.practicum.avro.mapper.SnapshotMapper;
import ru.yandex.practicum.avro.mapper.TimestampMapper;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnapshotRequestService {
    private final ScenarioRepository scenarioRepository;
    private final ConditionHandlerFactory conditionHandlerFactory;

    public List<DeviceActionRequest> prepareDeviceActions(SensorsSnapshotAvro sensorsSnapshot) {
        String hubId = sensorsSnapshot.getHubId();
        log.debug("Начало обработки снапшота с hubId = {}", hubId);

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            log.warn("Не найдено сценариев для хаба с id = {}. События для девайсов не будут созданы", hubId);
            return Collections.emptyList();
        }

        Map<String, SensorStateAvro> sensorsMap = sensorsSnapshot.getSensorsState();

        return scenarios.stream()
                .filter(scenario -> {
                    boolean isScenarioTriggered = checkIfScenarioTriggered(scenario, sensorsSnapshot, sensorsMap);
                    if (!isScenarioTriggered) {
                        log.debug("Сценарий {} не удовлетворяет условиям", scenario.getName());
                    }
                    return isScenarioTriggered;
                })
                .flatMap(scenario -> mapScenarioToDeviceActions(scenario, sensorsSnapshot, sensorsMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean checkIfScenarioTriggered(Scenario scenario,
                                             SensorsSnapshotAvro snapshot,
                                             Map<String, SensorStateAvro> sensorsMap) {
        if (!sensorsMap.containsKey(scenario.getHubId())) {
            log.debug("Нет состояния для хаб-устройства {}", scenario.getHubId());
            return false;
        }

        return scenario.getConditions().entrySet().stream()
                .allMatch(entry -> checkCondition(entry, snapshot, sensorsMap));
    }

    private boolean checkCondition(Map.Entry<String, Condition> conditionEntry,
                                   SensorsSnapshotAvro snapshot,
                                   Map<String, SensorStateAvro> sensorsMap) {
        String sensorId = conditionEntry.getKey();
        SensorStateAvro sensor = sensorsMap.get(sensorId);

        if (sensor == null) {
            log.debug("Sensor {} not found for hub {}", sensorId, snapshot.getHubId());
            return false;
        }

        SensorStateAvro state = snapshot.getSensorsState().get(sensorId);
        if (state == null) {
            log.debug("State not found for sensor {}", sensorId);
            return false;
        }

        return conditionHandlerFactory.getHandler(SnapshotMapper.mapSnapshotToSensorType(state.getData()))
                .isTriggered(conditionEntry.getValue(), state);
    }

    private Stream<DeviceActionRequest> mapScenarioToDeviceActions(Scenario scenario,
                                                                   SensorsSnapshotAvro snapshot,
                                                                   Map<String, SensorStateAvro> sensorsMap) {
        return scenario.getActions().entrySet().stream()
                .filter(entry -> sensorsMap.containsKey(entry.getKey()))
                .map(entry -> createDeviceActionRequest(
                        snapshot.getHubId(),
                        snapshot.getTimestamp(),
                        scenario.getName(),
                        entry.getKey(),
                        entry.getValue()
                ))
                .filter(Objects::nonNull);
    }

    private DeviceActionRequest createDeviceActionRequest(String hubId,
                                                          Instant timestamp,
                                                          String scenarioName,
                                                          String sensorId,
                                                          Action action) {
        if (action.getType() == ActionTypeAvro.SET_VALUE && action.getValue() == null) {
            log.warn("Действие SET_VALUE требует значение для сенсора {} в сценарии {}, пропускаю...", sensorId, scenarioName);
            return null;
        }

        DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(mapActionType(action.getType()));

        if (action.getValue() != null) {
            actionBuilder.setValue(action.getValue());
        }

        return DeviceActionRequest.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenarioName)
                .setAction(actionBuilder)
                .setTimestamp(TimestampMapper.mapToProto(timestamp))
                .build();
    }

    private ActionTypeProto mapActionType(ActionTypeAvro actionType) {
        return ActionTypeProto.valueOf(actionType.name());
    }
}