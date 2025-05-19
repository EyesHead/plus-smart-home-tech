package ru.yandex.practicum.analyzer.snapshot.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.event.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

@Component
@Slf4j
public class MotionConditionHandler implements ConditionHandler {
    @Override
    public boolean isTriggered(Condition condition, SensorStateAvro sensorData) {
        MotionSensorAvro currentSensorData = (MotionSensorAvro) sensorData.getData();

        boolean sensorValue = switch (condition.getType()) {
            case MOTION -> currentSensorData.getMotion();
            default -> throw new IllegalArgumentException("Несуществующий тип показателя для датчика движения: " + condition.getType());
        };

        boolean conditionValue = switch (condition.getValue()) {
            case 1 -> true;
            case 0 -> false;
            default -> throw new IllegalStateException("Невозможное значение поля value у condition для датчика движения: " + condition.getValue());
        };

        log.debug("Данные датчика движения, данные для удовлетворения условию, оператор EQUALS: {}, {}",
                sensorValue, conditionValue);

        return switch (condition.getOperation()) {
            case ConditionOperationAvro.EQUALS -> sensorValue == conditionValue;
            default -> throw new IllegalArgumentException("Несуществующая операция сравнения для датчика движения: " + condition.getType());
        };
    }

    @Override
    public DeviceTypeAvro getDeviceType() {
        return DeviceTypeAvro.MOTION_SENSOR;
    }
}
