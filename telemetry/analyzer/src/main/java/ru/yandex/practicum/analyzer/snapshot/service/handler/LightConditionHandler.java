package ru.yandex.practicum.analyzer.snapshot.service.handler;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.hub.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

@Component
public class LightConditionHandler implements ConditionHandler {
    @Override
    public boolean handle(Condition condition, SensorStateAvro sensorData) {
        LightSensorAvro luminosityData = (LightSensorAvro) sensorData.getData();

        int luminosity = switch (condition.getType()) {
            case LUMINOSITY -> luminosityData.getLuminosity();
            default -> throw new IllegalArgumentException("Несуществующий тип показателя для сенсора света: " + condition.getType());
        };

        return switch (condition.getOperation()) {
            case ConditionOperationAvro.GREATER_THAN -> luminosityData.getLuminosity() > luminosity;
            case ConditionOperationAvro.LOWER_THAN -> luminosityData.getLuminosity() < luminosity;
            case ConditionOperationAvro.EQUALS -> luminosityData.getLuminosity() == luminosity;
        };
    }

    @Override
    public DeviceTypeAvro getDeviceType() {
        return DeviceTypeAvro.LIGHT_SENSOR;
    }
}
