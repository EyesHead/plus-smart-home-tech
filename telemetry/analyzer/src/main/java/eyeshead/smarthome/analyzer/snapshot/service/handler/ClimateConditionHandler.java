package eyeshead.smarthome.analyzer.snapshot.service.handler;

import eyeshead.smarthome.analyzer.hubevent.model.Condition;
import eyeshead.smarthome.kafka.telemetry.event.ClimateSensorAvro;
import eyeshead.smarthome.kafka.telemetry.event.ConditionOperationAvro;
import eyeshead.smarthome.kafka.telemetry.event.DeviceTypeAvro;
import eyeshead.smarthome.kafka.telemetry.event.SensorStateAvro;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClimateConditionHandler implements ConditionHandler {
    @Override
    public boolean isTriggered(Condition condition, SensorStateAvro sensorData) {
        ClimateSensorAvro climateData = (ClimateSensorAvro) sensorData.getData();

        int sensorValue = switch (condition.getType()) {
            case TEMPERATURE -> climateData.getTemperatureC();
            case CO2LEVEL -> climateData.getCo2Level();
            case HUMIDITY -> climateData.getHumidity();
            default -> throw new IllegalArgumentException("Несуществующий тип показателя для сенсора климата: " + condition.getType());
        };

        Integer conditionValue = condition.getValue();
        if (conditionValue == null) {
            throw new IllegalArgumentException("Значение поля value для condition датчика климата не может быть null");
        }

        return switch (condition.getOperation()) {
            case ConditionOperationAvro.GREATER_THAN -> sensorValue > conditionValue;
            case ConditionOperationAvro.LOWER_THAN -> sensorValue < conditionValue;
            case ConditionOperationAvro.EQUALS -> sensorValue == conditionValue;
        };
    }

    @Override
    public DeviceTypeAvro getDeviceType() {
        return DeviceTypeAvro.CLIMATE_SENSOR;
    }
}
