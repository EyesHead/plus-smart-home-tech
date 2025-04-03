package ru.yandex.practicum.analyzer.snapshot.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.hub.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;

@Component
public class SwitchConditionHandler implements ConditionHandler {
    @Override
    public boolean handle(Condition condition, SensorStateAvro sensorData) {
        SwitchSensorAvro currentSensorData = (SwitchSensorAvro) sensorData.getData();

        boolean sensorValue = switch (condition.getType()) {
            case SWITCH -> currentSensorData.getState();
            default -> throw new IllegalArgumentException("Несуществующий тип показателя для сенсора переключателя: " + condition.getType());
        };

        boolean scenarioConditionValue = condition.getValue().equals(1);

        return switch (condition.getOperation()) {
            case ConditionOperationAvro.GREATER_THAN, ConditionOperationAvro.LOWER_THAN ->
                    throw new IllegalArgumentException("Несуществующая операция сравнения для сенсора переключателя: " + condition.getType());
            case ConditionOperationAvro.EQUALS -> sensorValue == scenarioConditionValue;
        };
    }

    @Override
    public DeviceTypeAvro getDeviceType() {
        return DeviceTypeAvro.SWITCH_SENSOR;
    }
}
