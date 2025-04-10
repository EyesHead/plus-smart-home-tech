package ru.yandex.practicum.analyzer.snapshot.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.hub.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

@Component
@Slf4j
public class LightConditionHandler implements ConditionHandler {
    @Override
    public boolean isTriggered(Condition condition, SensorStateAvro sensorData) {
        LightSensorAvro luminosityData = (LightSensorAvro) sensorData.getData();

        int sensorValue = switch (condition.getType()) {
            case LUMINOSITY -> luminosityData.getLuminosity();
            default -> throw new IllegalArgumentException("Несуществующий тип показателя для сенсора света: " + condition.getType());
        };

        int conditionValue = luminosityData.getLuminosity();

        log.debug("Сравниваем данные датчика света = {} с данными операции условия = {} по полю LUMINOSITY и оператором {}",
                sensorValue, conditionValue, condition.getOperation());

        return switch (condition.getOperation()) {
            case ConditionOperationAvro.GREATER_THAN -> sensorValue > conditionValue;
            case ConditionOperationAvro.LOWER_THAN -> sensorValue < conditionValue;
            case ConditionOperationAvro.EQUALS -> sensorValue == conditionValue;
        };
    }

    @Override
    public DeviceTypeAvro getDeviceType() {
        return DeviceTypeAvro.LIGHT_SENSOR;
    }
}
