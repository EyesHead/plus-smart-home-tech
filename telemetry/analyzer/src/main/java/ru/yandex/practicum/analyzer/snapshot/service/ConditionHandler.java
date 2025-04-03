package ru.yandex.practicum.analyzer.snapshot.service;

import ru.yandex.practicum.analyzer.hub.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

public interface ConditionHandler {
    boolean handle(Condition condition, SensorStateAvro sensorData);

    DeviceTypeAvro getDeviceType();
}