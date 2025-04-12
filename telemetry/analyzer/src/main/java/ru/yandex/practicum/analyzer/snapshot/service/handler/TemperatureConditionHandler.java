package ru.yandex.practicum.analyzer.snapshot.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.event.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

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

        log.debug("Сравниваем данные датчика температуры (по цельсию) = {} с данными операции условия (по цельсию) = {} по полю TEMPERATURE и оператором {}",
                sensorTempC, conditionTempC, condition.getOperation());


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
