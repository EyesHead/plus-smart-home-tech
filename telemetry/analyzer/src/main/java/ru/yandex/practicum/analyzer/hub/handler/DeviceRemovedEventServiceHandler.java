package ru.yandex.practicum.analyzer.hub.handler;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.hub.model.Sensor;
import ru.yandex.practicum.analyzer.hub.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Component
@RequiredArgsConstructor
public class DeviceRemovedEventServiceHandler implements HubEventServiceHandler {
    private final SensorRepository sensorRepository;

    @Override
    @Transactional
    public void handleEvent(HubEventAvro hubEventAvro) {
        DeviceRemovedEventAvro event = (DeviceRemovedEventAvro) hubEventAvro.getPayload();
        String sensorId = event.getId();

        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new EntityNotFoundException("Sensor not found: " + sensorId));

        // Все связи удалятся автоматически благодаря orphanRemoval
        sensorRepository.delete(sensor);
    }

    @Override
    public HubEventServiceType getHandlerType() {
        return HubEventServiceType.DEVICE_REMOVED_EVENT;
    }
}
