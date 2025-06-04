package eyeshead.smarthome.analyzer.snapshot.service.handler;

import eyeshead.smarthome.analyzer.hubevent.model.Condition;
import eyeshead.smarthome.kafka.telemetry.event.ConditionOperationAvro;
import eyeshead.smarthome.kafka.telemetry.event.DeviceTypeAvro;
import eyeshead.smarthome.kafka.telemetry.event.SensorStateAvro;
import eyeshead.smarthome.kafka.telemetry.event.SwitchSensorAvro;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SwitchConditionHandler implements ConditionHandler {
    @Override
    public boolean isTriggered(Condition condition, SensorStateAvro sensorData) {
        SwitchSensorAvro currentSensorData = (SwitchSensorAvro) sensorData.getData();

        boolean sensorValue = switch (condition.getType()) {
            case SWITCH -> currentSensorData.getState();
            default -> throw new IllegalArgumentException("Несуществующий тип показателя для сенсора переключателя: " +
                    condition.getType());
        };

        boolean conditionValue = condition.getValue().equals(1);

        if (condition.getOperation().equals(ConditionOperationAvro.EQUALS)) {
            return sensorValue == conditionValue;
        } else {
            throw new IllegalArgumentException("Несуществующая операция сравнения для сенсора переключателя: " +
                    condition.getType());
        }
    }

    @Override
    public DeviceTypeAvro getDeviceType() {
        return DeviceTypeAvro.SWITCH_SENSOR;
    }
}
