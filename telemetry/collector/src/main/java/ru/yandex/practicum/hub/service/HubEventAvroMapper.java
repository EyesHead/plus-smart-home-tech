package ru.yandex.practicum.hub.service;

import ru.yandex.practicum.hub.model.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;

public class HubEventAvroMapper {

    public static HubEventAvro toAvro(HubEvent hubEvent) {
        // Создаем билдер для основного события хаба
        HubEventAvro.Builder avroBuilder = HubEventAvro.newBuilder()
                .setHubId(hubEvent.getHubId())
                .setTimestamp(hubEvent.getTimestamp());

        // Обрабатываем конкретный тип события
        switch (hubEvent.getType()) {
            case DEVICE_ADDED -> {
                DeviceAddedEvent event = (DeviceAddedEvent) hubEvent;
                avroBuilder.setPayload(createDeviceAddedPayload(event));
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEvent event = (DeviceRemovedEvent) hubEvent;
                avroBuilder.setPayload(createDeviceRemovedPayload(event));
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEvent event = (ScenarioAddedEvent) hubEvent;
                avroBuilder.setPayload(createScenarioAddedPayload(event));
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEvent event = (ScenarioRemovedEvent) hubEvent;
                avroBuilder.setPayload(createScenarioRemovedPayload(event));
            }
            default -> throw new IllegalArgumentException("Unsupported hub event type: " + hubEvent.getType());
        }

        return avroBuilder.build();
    }

    private static DeviceAddedEventAvro createDeviceAddedPayload(DeviceAddedEvent event) {
        return DeviceAddedEventAvro.newBuilder()
                .setId(event.getId())
                .setType(mapDeviceType(event.getDeviceType()))
                .build();
    }

    private static DeviceRemovedEventAvro createDeviceRemovedPayload(DeviceRemovedEvent event) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(event.getId())
                .build();
    }

    private static ScenarioAddedEventAvro createScenarioAddedPayload(ScenarioAddedEvent event) {
        return ScenarioAddedEventAvro.newBuilder()
                .setName(event.getName())
                .setConditions(mapConditions(event.getConditions()))
                .setActions(mapActions(event.getActions()))
                .build();
    }

    private static ScenarioRemovedEventAvro createScenarioRemovedPayload(ScenarioRemovedEvent event) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(event.getName())
                .build();
    }

    private static List<ScenarioConditionAvro> mapConditions(List<ScenarioCondition> conditions) {
        return conditions.stream()
                .map(HubEventAvroMapper::mapCondition)
                .toList();
    }

    private static ScenarioConditionAvro mapCondition(ScenarioCondition condition) {
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(mapConditionType(condition.getType()))
                .setOperation(mapOperationType(condition.getOperation()));

        populateConditionValue(condition, builder);

        return builder.build();
    }

    private static void populateConditionValue(ScenarioCondition condition, ScenarioConditionAvro.Builder builder) {
        if (condition.getType() == ConditionType.SWITCH) {
            // Для SWITCH используем boolean значение
            Boolean boolValue = condition.getValue() != null ? condition.getValue() != 0 : null;
            builder.setValue(boolValue);
        } else {
            // Для остальных типов используем integer значение
            Integer intValue = condition.getValue();
            builder.setValue(intValue);
        }
    }

    private static List<DeviceActionAvro> mapActions(List<DeviceAction> actions) {
        return actions.stream()
                .map(HubEventAvroMapper::mapAction)
                .toList();
    }

    private static DeviceActionAvro mapAction(DeviceAction action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(mapActionType(action.getType()))
                .setValue(action.getValue() != null ? action.getValue() : null)
                .build();
    }

    // Маппинг enum-типов
    private static DeviceTypeAvro mapDeviceType(DeviceType type) {
        return DeviceTypeAvro.valueOf(type.name());
    }

    private static ConditionTypeAvro mapConditionType(ConditionType type) {
        return ConditionTypeAvro.valueOf(type.name());
    }

    private static ConditionOperationAvro mapOperationType(OperationType type) {
        return ConditionOperationAvro.valueOf(type.name());
    }

    private static ActionTypeAvro mapActionType(ActionType type) {
        return ActionTypeAvro.valueOf(type.name());
    }
}
