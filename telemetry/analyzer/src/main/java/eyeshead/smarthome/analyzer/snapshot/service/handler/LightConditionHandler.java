package eyeshead.smarthome.analyzer.snapshot.service.handler;

import eyeshead.smarthome.analyzer.hubevent.model.Condition;
import eyeshead.smarthome.kafka.telemetry.event.ConditionOperationAvro;
import eyeshead.smarthome.kafka.telemetry.event.DeviceTypeAvro;
import eyeshead.smarthome.kafka.telemetry.event.LightSensorAvro;
import eyeshead.smarthome.kafka.telemetry.event.SensorStateAvro;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
