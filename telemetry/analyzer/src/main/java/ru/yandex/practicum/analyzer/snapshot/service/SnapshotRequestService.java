package ru.yandex.practicum.analyzer.snapshot.service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.hub.model.Action;
import ru.yandex.practicum.analyzer.hub.model.Condition;
import ru.yandex.practicum.analyzer.hub.model.Scenario;
import ru.yandex.practicum.analyzer.hub.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.hub.repository.SensorRepository;
import ru.yandex.practicum.analyzer.snapshot.service.handler.ConditionHandlerFactory;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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

        return scenarioRepository.findByHubId(sensorsSnapshot.getHubId()).stream()
                .filter(scenario -> isScenarioTriggered(scenario, sensorsSnapshot))
                .flatMap(scenario -> mapScenarioToActions(scenario, sensorsSnapshot))
                .collect(Collectors.toList());
    }

    private boolean isScenarioTriggered(Scenario scenario, SensorsSnapshotAvro snapshotAvro) {
        return scenario.getConditions()
                .entrySet()
                .stream()
                .allMatch(entry -> checkCondition(entry, snapshotAvro));
    }

    private boolean checkCondition(Map.Entry<String, Condition> conditionEntry, SensorsSnapshotAvro snapshotAvro) {
        final String sensorId = conditionEntry.getKey();
        final Condition condition = conditionEntry.getValue();
        final Map<String, SensorStateAvro> sensorStates = snapshotAvro.getSensorsState();
        final String hubId = snapshotAvro.getHubId();

        return sensorRepository.findByIdAndHubId(sensorId, hubId)
                .map(sensor -> {
                    SensorStateAvro state = sensorStates.get(sensorId);
                    if (state != null) return false;

                    return conditionHandlerFactory.getHandler(sensor.getType())
                            .handle(condition, state);
                })
                .orElse(false);
    }

    private Stream<DeviceActionRequest> mapScenarioToActions(Scenario scenario, SensorsSnapshotAvro sensorsSnapshot) {
        return scenario.getActions().entrySet().stream()
                .map(entry -> buildActionRequestProto(
                        sensorsSnapshot.getHubId(),
                        scenario.getName(),
                        entry.getKey(),
                        entry.getValue(),
                        sensorsSnapshot.getTimestamp()
                ));
    }

    private DeviceActionRequest buildActionRequestProto(String hubId, String scenarioName,
                                                        String sensorId, Action action, Instant timestamp) {
        return DeviceActionRequest.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenarioName)
                .setAction(DeviceActionProto.newBuilder()
                        .setSensorId(sensorId)
                        .setType(mapActionType(action.getType()))
                        .setValue(action.getValue()))
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(timestamp.getEpochSecond())
                        .setNanos(timestamp.getNano()).build())
                .build();
    }

    private ActionTypeProto mapActionType(ActionTypeAvro actionType) {
        return ActionTypeProto.valueOf(actionType.name());
    }
}