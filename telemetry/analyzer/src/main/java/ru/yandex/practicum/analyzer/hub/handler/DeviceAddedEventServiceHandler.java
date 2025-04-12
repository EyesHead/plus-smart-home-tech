package ru.yandex.practicum.analyzer.hub.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.hub.model.Sensor;
import ru.yandex.practicum.analyzer.hub.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@RequiredArgsConstructor
@Component
@Slf4j
public class DeviceAddedEventServiceHandler implements HubEventServiceHandler {
    private final SensorRepository repository;

    @Override
    @Transactional
    public void handleEvent(HubEventAvro hubEventAvro) {
        Sensor sensor = createSensor(hubEventAvro);

        repository.save(sensor);
    }

    private Sensor createSensor(HubEventAvro hubEventAvro) {
        DeviceAddedEventAvro sensorData = (DeviceAddedEventAvro) hubEventAvro.getPayload();

        Sensor sensor = new Sensor();

        sensor.setHubId(hubEventAvro.getHubId());
        sensor.setType(sensorData.getType());
        sensor.setId(sensorData.getId());

        log.debug("HubEventAvro был переведен в Sensor и будет сохранён в БД: {}", sensor);

        return sensor;
    }

    @Override
    public HubEventServiceType getHandlerType() {
        return HubEventServiceType.DEVICE_ADDED_EVENT;
    }
}
