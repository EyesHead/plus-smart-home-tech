package ru.yandex.practicum.analyzer.hub.factory;

import ru.yandex.practicum.analyzer.hub.model.Action;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;

public interface ActionCreator {
    Action create(DeviceActionAvro actionAvro);
}
