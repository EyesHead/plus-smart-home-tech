package ru.yandex.practicum.analyzer.snapshot.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.event.model.Condition;
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

        int sensorValue = luminosityData.getLuminosity();
        Integer conditionValue = condition.getValue();

        if (conditionValue == null) {
            throw new IllegalArgumentException("Для условия LUMINOSITY значение не может быть null");
        }

        log.debug("Проверка LUMINOSITY: показание = {}, ожидаемое значение = {}, операция = {}",
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
