package ru.yandex.practicum.collector.hub.mapper;

import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

public class HubEventMapper {
    public static HubEventAvro map(HubEventProto proto) {
        return HubEventAvro.newBuilder()
                .setHubId(proto.getHubId())
                .setTimestamp(Instant.ofEpochSecond(proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos()))
                .setPayload(mapPayload(proto))
                .build();
    }

    private static Object mapPayload(HubEventProto proto) {
        return switch (proto.getPayloadCase()) {
            case DEVICE_ADDED -> DeviceAddedEventAvro.newBuilder()
                    .setId(proto.getDeviceAdded().getId())
                    .setType(DeviceTypeAvro.valueOf(proto.getDeviceAdded().getType().name()))
                    .build();
            case DEVICE_REMOVED -> DeviceRemovedEventAvro.newBuilder()
                    .setId(proto.getDeviceRemoved().getId())
                    .build();
            case SCENARIO_ADDED -> ScenarioAddedEventAvro.newBuilder()
                    .setName(proto.getScenarioAdded().getName())
                    .setConditions(proto.getScenarioAdded().getConditionList().stream()
                            .map(HubEventMapper::mapCondition)
                            .toList())
                    .setActions(proto.getScenarioAdded().getActionList().stream()
                            .map(HubEventMapper::mapAction)
                            .toList())
                    .build();
            case SCENARIO_REMOVED -> ScenarioRemovedEventAvro.newBuilder()
                    .setName(proto.getScenarioRemoved().getName())
                    .build();
            default -> throw new IllegalArgumentException("Unknown payload type");
        };
    }

    private static ScenarioConditionAvro mapCondition(ScenarioConditionProto proto) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(proto.getSensorId())
                .setType(ConditionTypeAvro.valueOf(proto.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(proto.getOperation().name()))
                .setValue(proto.hasBoolValue() ? proto.getBoolValue() : proto.hasIntValue() ? proto.getIntValue() : null)
                .build();
    }

    private static DeviceActionAvro mapAction(DeviceActionProto proto) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(proto.getSensorId())
                .setType(ActionTypeAvro.valueOf(proto.getType().name()))
                .setValue(proto.getValue())
                .build();
    }
}