package ru.yandex.practicum.analyzer.snapshot.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.hub.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

@Component
public class MotionConditionHandler implements ConditionHandler {
    @Override
    public boolean handle(Condition condition, SensorStateAvro sensorData) {
        MotionSensorAvro currentSensorData = (MotionSensorAvro) sensorData.getData();

        boolean sensorValue = switch (condition.getType()) {
            case MOTION -> currentSensorData.getMotion();
            default -> throw new IllegalArgumentException("Несуществующий тип показателя для датчика движения: " + condition.getType());
        };

        boolean scenarioConditionValue = condition.getValue().equals(1);

        return switch (condition.getOperation()) {
            case ConditionOperationAvro.EQUALS -> sensorValue == scenarioConditionValue;
            default -> throw new IllegalArgumentException("Несуществующая операция сравнения для датчика движения: " + condition.getType());
        };
    }

    @Override
    public DeviceTypeAvro getDeviceType() {
        return DeviceTypeAvro.MOTION_SENSOR;
    }
}
