package ru.yandex.practicum.analyzer.snapshot.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.hub.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Component
public class TemperatureConditionHandler implements ConditionHandler {
    @Override
    public boolean handle(Condition condition, SensorStateAvro sensorData) {
        TemperatureSensorAvro tempSensorData = (TemperatureSensorAvro) sensorData.getData();

        int sensorTemperatureC = switch (condition.getType()) {
            case TEMPERATURE -> tempSensorData.getTemperatureC();
            default -> throw new IllegalArgumentException("Несуществующий тип показателя для сенсора переключателя: " + condition.getType());
        };

        return switch (condition.getOperation()) {
            case ConditionOperationAvro.GREATER_THAN -> tempSensorData.getTemperatureC() > sensorTemperatureC;
            case ConditionOperationAvro.LOWER_THAN -> tempSensorData.getTemperatureC() < sensorTemperatureC;
            case ConditionOperationAvro.EQUALS -> tempSensorData.getTemperatureC() == sensorTemperatureC;
        };
    }

    @Override
    public DeviceTypeAvro getDeviceType() {
        return DeviceTypeAvro.TEMPERATURE_SENSOR;
    }
}
