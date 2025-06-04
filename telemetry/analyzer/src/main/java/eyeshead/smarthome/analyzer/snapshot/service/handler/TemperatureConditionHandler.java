package eyeshead.smarthome.analyzer.snapshot.service.handler;

import eyeshead.smarthome.analyzer.hubevent.model.Condition;
import eyeshead.smarthome.kafka.telemetry.event.ConditionOperationAvro;
import eyeshead.smarthome.kafka.telemetry.event.DeviceTypeAvro;
import eyeshead.smarthome.kafka.telemetry.event.SensorStateAvro;
import eyeshead.smarthome.kafka.telemetry.event.TemperatureSensorAvro;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TemperatureConditionHandler implements ConditionHandler {
    @Override
    public boolean isTriggered(Condition condition, SensorStateAvro sensorData) {
        TemperatureSensorAvro tempSensorData = (TemperatureSensorAvro) sensorData.getData();

        int sensorTempC = tempSensorData.getTemperatureC();

        int conditionTempC = switch (condition.getType()) {
            case TEMPERATURE -> condition.getValue();
            default -> throw new IllegalArgumentException("Несуществующий тип показателя для сенсора переключателя: " + condition.getType());
        };

        return switch (condition.getOperation()) {
            case ConditionOperationAvro.GREATER_THAN -> sensorTempC > conditionTempC;
            case ConditionOperationAvro.LOWER_THAN -> sensorTempC < conditionTempC;
            case ConditionOperationAvro.EQUALS -> sensorTempC == conditionTempC;
        };
    }

    @Override
    public DeviceTypeAvro getDeviceType() {
        return DeviceTypeAvro.TEMPERATURE_SENSOR;
    }
}
