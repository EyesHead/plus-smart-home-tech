package ru.yandex.practicum.analyzer.event.handler;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.event.model.Sensor;
import ru.yandex.practicum.analyzer.event.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceRemovedEventServiceHandler implements HubEventServiceHandler {
    private final SensorRepository sensorRepository;

    @Override
    @Transactional
    public void handleEvent(HubEventAvro hubEventAvro) {
        DeviceRemovedEventAvro event = (DeviceRemovedEventAvro) hubEventAvro.getPayload();
        String sensorId = event.getId();

        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new EntityNotFoundException("Sensor not found: " + sensorId));

        log.debug("Sensor будет удалён из БД. {}", sensor);

        // Все связи удалятся автоматически благодаря orphanRemoval
        sensorRepository.deleteById(sensorId);
    }

    @Override
    public HubEventServiceType getHandlerType() {
        return HubEventServiceType.DEVICE_REMOVED_EVENT;
    }
}
