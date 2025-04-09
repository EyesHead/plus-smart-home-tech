package ru.yandex.practicum.analyzer.hub.handler;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public interface HubEventServiceHandler {
    void handleEvent(HubEventAvro hubEventAvro);

    HubEventServiceType getHandlerType();
}
