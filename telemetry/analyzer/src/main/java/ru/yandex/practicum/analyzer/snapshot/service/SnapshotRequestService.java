package ru.yandex.practicum.analyzer.snapshot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.event.model.Action;
import ru.yandex.practicum.analyzer.event.model.Condition;
import ru.yandex.practicum.analyzer.event.model.Scenario;
import ru.yandex.practicum.analyzer.event.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.snapshot.service.handler.ConditionHandler;
import ru.yandex.practicum.analyzer.snapshot.service.handler.ConditionHandlerFactory;
import ru.yandex.practicum.avro.mapper.SnapshotMapper;
import ru.yandex.practicum.avro.mapper.TimestampMapper;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnapshotRequestService {
    private final ScenarioRepository scenarioRepository;
    private final ConditionHandlerFactory conditionHandlerFactory;

    public List<DeviceActionRequest> prepareDeviceActions(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        log.debug("Получен снапшот от хаба {} :\n{}", snapshot.getHubId(), snapshot);

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            log.warn("Не найдено сценариев для хаба с id = {}. События для девайсов не будут созданы", hubId);
            return Collections.emptyList();
        }

        log.debug("Найдено {} сценариев для хаба {}", scenarios.size(), hubId);
        scenarios.forEach(s -> log.debug("→ Сценарий: name={}, conditions={}, actions={}",
                s.getName(), s.getConditions().values(), s.getActions().values()));

        return scenarios.stream()
                .filter(scenario -> {
                    boolean isScenarioTriggered = checkIfScenarioTriggered(scenario, snapshot);
                    if (!isScenarioTriggered) {
                        log.debug("Показания снапшота НЕ удовлетворяют условиям активации сценария '{}'", scenario.getName());
                    } else {
                        log.debug("Показания снапшота УДОВЛЕТВОРЯЮТ условиям активации сценария '{}'", scenario.getName());
                    }
                    return isScenarioTriggered;
                })
                .flatMap(scenario -> toDeviceActionsFromScenario(scenario, snapshot))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean checkIfScenarioTriggered(Scenario scenario, SensorsSnapshotAvro snapshot) {
        Set<String> snapshotSensorIds = snapshot.getSensorsState().keySet();
        Set<String> conditionSensorIds = scenario.getConditions().keySet();

        if (!snapshotSensorIds.equals(conditionSensorIds)) {
            log.warn("Id сенсоров в снапшоте не совпадают с id сенсоров в условиях сценария '{}'. Снапшот: {}, сценарий: {}",
                    scenario.getName(), snapshotSensorIds, conditionSensorIds);
            return false;
        }

        return scenario.getConditions().entrySet().stream()
                .allMatch(entry -> checkCondition(entry, snapshot));
    }


    private boolean checkCondition(Map.Entry<String, Condition> conditionEntry, SensorsSnapshotAvro snapshot) {
        String sensorId = conditionEntry.getKey();
        Condition condition = conditionEntry.getValue();
        SensorStateAvro state = snapshot.getSensorsState().get(sensorId);

        log.debug("Проверка условия для сенсора '{}': условие={}, показание={}", sensorId, condition, state.getData());

        DeviceTypeAvro deviceType = SnapshotMapper.mapSnapshotToSensorType(state.getData());
        ConditionHandler handler = conditionHandlerFactory.getHandler(deviceType);

        log.debug("→ Обработчик для сенсора '{}': тип устройства = {}, handler = {}",
                sensorId, deviceType, handler.getClass().getSimpleName());

        boolean result = handler.isTriggered(condition, state);

        log.debug("→ Результат проверки: {}", result ? "ПРОЙДЕНО" : "НЕ ПРОЙДЕНО");
        return result;
    }

    private Stream<DeviceActionRequest> toDeviceActionsFromScenario(Scenario scenario,
                                                                    SensorsSnapshotAvro snapshot) {
        return scenario.getActions()
                .entrySet()
                .stream()
                .map(sensorIdActionEntry -> createDeviceActionRequest(
                        snapshot.getHubId(),
                        snapshot.getTimestamp(),
                        scenario.getName(),
                        sensorIdActionEntry.getKey(),
                        sensorIdActionEntry.getValue()
                ));
    }

    private DeviceActionRequest createDeviceActionRequest(String hubId,
                                                          Instant timestamp,
                                                          String scenarioName,
                                                          String sensorId,
                                                          Action action) {
        if (!isValidAction(action, sensorId, scenarioName)) return null;

        DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(mapActionType(action.getType()));

        if (action.getValue() != null) {
            actionBuilder.setValue(action.getValue());
        }

        return DeviceActionRequest.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenarioName)
                .setAction(actionBuilder.build())
                .setTimestamp(TimestampMapper.mapToProto(timestamp))
                .build();
    }

    private boolean isValidAction(Action action, String sensorId, String scenarioName) {
        if (action.getType() == ActionTypeAvro.SET_VALUE && action.getValue() == null) {
            log.warn("Действие SET_VALUE требует значение для сенсора {} в сценарии '{}', пропускаю...", sensorId, scenarioName);
            return false;
        }
        return true;
    }

    private ActionTypeProto mapActionType(ActionTypeAvro actionType) {
        return ActionTypeProto.valueOf(actionType.name());
    }
}