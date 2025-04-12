package ru.yandex.practicum.analyzer.snapshot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.event.model.Action;
import ru.yandex.practicum.analyzer.event.model.Condition;
import ru.yandex.practicum.analyzer.event.model.Scenario;
import ru.yandex.practicum.analyzer.event.model.Sensor;
import ru.yandex.practicum.analyzer.event.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.event.repository.SensorRepository;
import ru.yandex.practicum.analyzer.snapshot.service.handler.ConditionHandlerFactory;
import ru.yandex.practicum.avro.mapper.TimestampMapper;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnapshotRequestService {
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionHandlerFactory conditionHandlerFactory;

    public List<DeviceActionRequest> prepareDeviceActions(SensorsSnapshotAvro sensorsSnapshot) {
        String hubId = sensorsSnapshot.getHubId();
        log.debug("Processing snapshot for hub: {}", hubId);

        // Получаем все сценарии для хаба
        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            log.debug("No scenarios found for hub {}", hubId);
            return Collections.emptyList();
        }

        // Собираем все ID сенсоров из условий и действий всех сценариев
        Set<String> allSensorIds = collectAllSensorIds(scenarios);
        if (allSensorIds.isEmpty()) {
            log.debug("No sensors found in scenarios for hub {}", hubId);
            return Collections.emptyList();
        }

        // Получаем все сенсоры одним запросом с EntityGraph
        Map<String, Sensor> sensorsWithIds = sensorRepository
                .findSensorsByIdsAndHubId(allSensorIds, hubId)
                .stream()
                .collect(Collectors.toMap(Sensor::getId, Function.identity()));

        log.debug("Было найдено {} датчика у хаба с hubId = {}", sensorsWithIds.size(), hubId);

        // Обрабатываем сценарии
        return scenarios.stream()
                .filter(scenario -> isScenarioTriggered(scenario, sensorsSnapshot, sensorsWithIds))
                .peek(scenario -> log.debug("Scenario '{}' triggered", scenario.getName()))
                .flatMap(scenario -> mapScenarioActionsToResponseActions(scenario, sensorsSnapshot, sensorsWithIds))
                .peek(deviceActionRequest -> log.debug("DeviceActionRequest был создан: {}", deviceActionRequest))
                .collect(Collectors.toList());
    }

    private Set<String> collectAllSensorIds(List<Scenario> scenarios) {
        Set<String> sensorIds = new HashSet<>();
        scenarios.forEach(scenario -> {
            sensorIds.addAll(scenario.getConditions().keySet());
            sensorIds.addAll(scenario.getActions().keySet());
        });
        return sensorIds;
    }

    private boolean isScenarioTriggered(Scenario scenario,
                                        SensorsSnapshotAvro snapshotAvro,
                                        Map<String, Sensor> sensorsMap) {
        return scenario.getConditions().entrySet().stream()
                .allMatch(entry -> checkCondition(entry, snapshotAvro, sensorsMap));
    }

    private boolean checkCondition(Map.Entry<String, Condition> conditionEntry,
                                   SensorsSnapshotAvro snapshotAvro,
                                   Map<String, Sensor> sensorsMap) {
        String sensorId = conditionEntry.getKey();
        Sensor sensor = sensorsMap.get(sensorId);

        if (sensor == null) {
            log.debug("Sensor {} not found for hub {}", sensorId, snapshotAvro.getHubId());
            return false;
        }

        SensorStateAvro state = snapshotAvro.getSensorsState().get(sensorId);
        if (state == null) {
            log.debug("State not found for sensor {}", sensorId);
            return false;
        }

        boolean triggered = conditionHandlerFactory.getHandler(sensor.getType())
                .isTriggered(conditionEntry.getValue(), state);

        log.debug("Condition check for sensor {}: {}", sensorId, triggered);
        return triggered;
    }

    private Stream<DeviceActionRequest> mapScenarioActionsToResponseActions(
            Scenario scenario,
            SensorsSnapshotAvro sensorsSnapshot,
            Map<String, Sensor> sensorsMap) {

        return scenario.getActions().entrySet().stream()
                .filter(entry -> {
                    boolean exists = sensorsMap.containsKey(entry.getKey());
                    if (!exists) {
                        log.debug("Action sensor {} not found, skipping action", entry.getKey());
                    }
                    return exists;
                })
                .map(entry -> buildActionRequestProto(
                        sensorsSnapshot.getHubId(),
                        sensorsSnapshot.getTimestamp(),
                        scenario.getName(),
                        entry.getKey(),
                        entry.getValue()
                ));
    }

    private DeviceActionRequest buildActionRequestProto(String hubId,
                                                        Instant timestamp,
                                                        String scenarioName,
                                                        String sensorId,
                                                        Action action) {
        DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(mapActionType(action.getType()));

        // Для SET_VALUE обязательно должно быть значение
        if (action.getType() == ActionTypeAvro.SET_VALUE) {
            if (action.getValue() == null) {
                log.error("SET_VALUE action requires value for sensor {} in scenario {}",
                        sensorId, scenarioName);
                return null;
            }
            actionBuilder.setValue(action.getValue());
        }
        // Для других типов действий значение необязательно
        else if (action.getValue() != null) {
            actionBuilder.setValue(action.getValue());
        }

        // Если значение null и тип не SET_VALUE - не устанавливаем value вообще
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