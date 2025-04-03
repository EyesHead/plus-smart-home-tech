package ru.yandex.practicum.analyzer.snapshot.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.hub.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

@Component
public class ClimateConditionHandler implements ConditionHandler {
    @Override
    public boolean handle(Condition condition, SensorStateAvro sensorData) {
        ClimateSensorAvro currentSensorData = (ClimateSensorAvro) sensorData.getData();

        int sensorValue = switch (condition.getType()) {
            case TEMPERATURE -> currentSensorData.getTemperatureC();
            case CO2LEVEL -> currentSensorData.getCo2Level();
            case HUMIDITY -> currentSensorData.getHumidity();
            default -> throw new IllegalArgumentException("Несуществующий тип показателя для сенсора климата: " + condition.getType());
        };

        Integer scenarioConditionValue = condition.getValue();
        return switch (condition.getOperation()) {
            case ConditionOperationAvro.GREATER_THAN -> sensorValue > scenarioConditionValue;
            case ConditionOperationAvro.LOWER_THAN -> sensorValue < scenarioConditionValue;
            case ConditionOperationAvro.EQUALS -> sensorValue == scenarioConditionValue;
        };
    }

    @Override
    public DeviceTypeAvro getDeviceType() {
        return DeviceTypeAvro.CLIMATE_SENSOR;
    }
}
