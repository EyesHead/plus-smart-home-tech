package ru.yandex.practicum.hub.mapper;

import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;

public class HubEventAvroMapper {

    public static HubEventAvro toAvro(HubEventProto event) {
        // Создаем билдер для основного события хаба
        Instant timestamp = Instant.ofEpochSecond(event.getTimestamp().getSeconds());

        HubEventAvro.Builder avroBuilder = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(timestamp);

        // Обрабатываем конкретный тип события
        switch (event.getPayloadCase()) {
            case HubEventProto.PayloadCase.DEVICE_ADDED -> avroBuilder
                    .setPayload(createDeviceAddedPayload(event.getDeviceAdded()));
            case HubEventProto.PayloadCase.DEVICE_REMOVED -> avroBuilder
                    .setPayload(createDeviceRemovedPayload(event.getDeviceRemoved()));
            case HubEventProto.PayloadCase.SCENARIO_ADDED -> avroBuilder
                    .setPayload(createScenarioAddedPayload(event.getScenarioAdded()));
            case HubEventProto.PayloadCase.SCENARIO_REMOVED -> avroBuilder
                    .setPayload(createScenarioRemovedPayload(event.getScenarioRemoved()));
            default -> throw new IllegalArgumentException("Unsupported hub event type: " + event.getPayloadCase());
        }

        return avroBuilder.build();
    }

    private static DeviceAddedEventAvro createDeviceAddedPayload(DeviceAddedEventProto event) {
        return DeviceAddedEventAvro.newBuilder()
                .setId(event.getId())
                .setType(mapDeviceType(event.getType()))
                .build();
    }

    private static DeviceRemovedEventAvro createDeviceRemovedPayload(DeviceRemovedEventProto event) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(event.getId())
                .build();
    }

    private static ScenarioAddedEventAvro createScenarioAddedPayload(ScenarioAddedEventProto event) {
        return ScenarioAddedEventAvro.newBuilder()
                .setName(event.getName())
                .setConditions(mapConditions(event.getConditionList()))
                .setActions(mapActions(event.getActionList()))
                .build();
    }

    private static ScenarioRemovedEventAvro createScenarioRemovedPayload(ScenarioRemovedEventProto event) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(event.getName())
                .build();
    }

    private static List<ScenarioConditionAvro> mapConditions(List<ScenarioConditionProto> conditions) {
        return conditions.stream()
                .map(HubEventAvroMapper::mapCondition)
                .toList();
    }

    private static ScenarioConditionAvro mapCondition(ScenarioConditionProto condition) {
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(mapConditionType(condition.getType()))
                .setOperation(mapOperationType(condition.getOperation()));

        populateConditionValue(condition, builder);

        return builder.build();
    }

    private static void populateConditionValue(ScenarioConditionProto condition, ScenarioConditionAvro.Builder builder) {
        if (condition.getType() == ConditionTypeProto.SWITCH) {
            // Для SWITCH используем boolean значение
            Boolean boolValue = condition.getBoolValue();
            builder.setValue(boolValue);
        } else {
            // Для остальных типов используем integer значение
            Integer intValue = condition.getIntValue();
            builder.setValue(intValue);
        }
    }

    private static List<DeviceActionAvro> mapActions(List<DeviceActionProto> actions) {
        return actions.stream()
                .map(HubEventAvroMapper::mapAction)
                .toList();
    }

    private static DeviceActionAvro mapAction(DeviceActionProto action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(mapActionType(action.getType()))
                .setValue(action.getValue())
                .build();
    }

    // Маппинг enum-типов
    private static DeviceTypeAvro mapDeviceType(DeviceTypeProto type) {
        return DeviceTypeAvro.valueOf(type.name());
    }

    private static ConditionTypeAvro mapConditionType(ConditionTypeProto type) {
        return ConditionTypeAvro.valueOf(type.name());
    }

    private static ConditionOperationAvro mapOperationType(ConditionOperationProto type) {
        return ConditionOperationAvro.valueOf(type.name());
    }

    private static ActionTypeAvro mapActionType(ActionTypeProto type) {
        return ActionTypeAvro.valueOf(type.name());
    }
}