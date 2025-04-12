package ru.yandex.practicum.analyzer.snapshot.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.event.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;

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

        log.debug("Сравниваем данные датчика переключателя = {} с данными операции условия = {} по полю SWITCH и оператором {}",
                sensorValue, conditionValue, condition.getOperation());

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
