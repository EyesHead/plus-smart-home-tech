package eyeshead.smarthome.analyzer.snapshot.service.handler;

import eyeshead.smarthome.analyzer.hubevent.model.Condition;
import eyeshead.smarthome.kafka.telemetry.event.ConditionOperationAvro;
import eyeshead.smarthome.kafka.telemetry.event.DeviceTypeAvro;
import eyeshead.smarthome.kafka.telemetry.event.MotionSensorAvro;
import eyeshead.smarthome.kafka.telemetry.event.SensorStateAvro;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
