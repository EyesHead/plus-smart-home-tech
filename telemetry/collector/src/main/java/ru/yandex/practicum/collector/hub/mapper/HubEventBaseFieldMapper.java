package ru.yandex.practicum.collector.hub.mapper;

import ru.yandex.practicum.avro.mapper.TimestampMapper;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.*;

public class HubEventBaseFieldMapper {
    public static HubEventAvro.Builder map(HubEventProto proto) {
        return HubEventAvro.newBuilder()
                .setHubId(proto.getHubId())
                .setTimestamp(TimestampMapper.mapToInstant(proto.getTimestamp()));
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
                            .map(HubEventBaseFieldMapper::mapCondition)
                            .toList())
                    .setActions(proto.getScenarioAdded().getActionList().stream()
                            .map(HubEventBaseFieldMapper::mapAction)
                            .toList())
                    .build();
            case SCENARIO_REMOVED -> ScenarioRemovedEventAvro.newBuilder()
                    .setName(proto.getScenarioRemoved().getName())
                    .build();
            default -> throw new IllegalArgumentException("Unknown payload type");
        };
    }

    private static ScenarioConditionAvro mapCondition(ScenarioConditionProto condition) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .setValue(
                        condition.hasBoolValue() ? Integer.valueOf(1) :
                                condition.hasIntValue() ? condition.getIntValue() :
                                        null
                )
                .build();
    }

    private static DeviceActionAvro mapAction(DeviceActionProto proto) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(proto.getSensorId())
                .setType(ActionTypeAvro.valueOf(proto.getType().name()))
                .setValue(proto.hasValue() ? proto.getValue() : null)
                .build();
    }
}