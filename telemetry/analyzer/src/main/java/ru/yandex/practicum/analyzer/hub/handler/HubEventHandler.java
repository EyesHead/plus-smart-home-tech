package ru.yandex.practicum.analyzer.hub.handler;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public interface HubEventHandler {
    void handle(HubEventAvro hubEventAvro);

    HubEventHandlerType getHandlerType();
}
