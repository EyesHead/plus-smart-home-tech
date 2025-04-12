package ru.yandex.practicum.analyzer.event.service.handler;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public interface HubEventServiceHandler {
    void handleEvent(HubEventAvro hubEventAvro);

    HubEventServiceType getHandlerType();
}
