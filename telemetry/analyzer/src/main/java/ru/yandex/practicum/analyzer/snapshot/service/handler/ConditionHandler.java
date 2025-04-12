package ru.yandex.practicum.analyzer.snapshot.service.handler;

import ru.yandex.practicum.analyzer.event.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

public interface ConditionHandler {
    boolean isTriggered(Condition condition, SensorStateAvro sensorData);

    DeviceTypeAvro getDeviceType();
}