package eyeshead.smarthome.analyzer.hubevent.service.handler;

import eyeshead.smarthome.analyzer.hubevent.model.Sensor;
import eyeshead.smarthome.analyzer.hubevent.repository.SensorRepository;
import eyeshead.smarthome.kafka.telemetry.event.DeviceRemovedEventAvro;
import eyeshead.smarthome.kafka.telemetry.event.HubEventAvro;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new EntityNotFoundException("Сенсор с id = {} не найден в БД: " + sensorId));

        log.debug("Sensor с id = {} будет удалён из БД", sensor.getId());

        // Все связи удалятся автоматически благодаря orphanRemoval
        sensorRepository.delete(sensor);
    }

    @Override
    public HubEventServiceType getHandlerType() {
        return HubEventServiceType.DEVICE_REMOVED_EVENT;
    }
}
