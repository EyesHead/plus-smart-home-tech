package ru.yandex.practicum.analyzer.snapshot.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.event.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

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

        log.debug("Данные датчика климата, данные для удовлетворения условию, оператор: {}, {}, {}",
                sensorValue, conditionValue, condition.getOperation());
        return switch (condition.getOperation()) {
            case ConditionOperationAvro.GREATER_THAN -> conditionValue > sensorValue;
            case ConditionOperationAvro.LOWER_THAN -> conditionValue < sensorValue;
            case ConditionOperationAvro.EQUALS -> conditionValue == sensorValue;
        };
    }

    @Override
    public DeviceTypeAvro getDeviceType() {
        return DeviceTypeAvro.CLIMATE_SENSOR;
    }
}
