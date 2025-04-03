package ru.yandex.practicum.analyzer.hub.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.hub.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Component
@RequiredArgsConstructor
public class DeviceRemovedEventHandler implements HubEventHandler {
    private final SensorRepository repository;

    @Override
    public void handle(HubEventAvro hubEventAvro) {
        DeviceRemovedEventAvro eventExtracted = (DeviceRemovedEventAvro) hubEventAvro.getPayload();
        repository.deleteById(eventExtracted.getId());
    }

    @Override
    public HubEventHandlerType getHandlerType() {
        return HubEventHandlerType.DEVICE_REMOVED_EVENT;
    }
}
