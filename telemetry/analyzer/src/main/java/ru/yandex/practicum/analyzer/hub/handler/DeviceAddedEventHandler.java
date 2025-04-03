package ru.yandex.practicum.analyzer.hub.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.hub.factory.SensorFactory;
import ru.yandex.practicum.analyzer.hub.model.Sensor;
import ru.yandex.practicum.analyzer.hub.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@RequiredArgsConstructor
@Component
public class DeviceAddedEventHandler implements HubEventHandler {
    private final SensorRepository repository;

    @Override
    public void handle(HubEventAvro hubEventAvro) {
        Sensor sensor = SensorFactory.createSensor(hubEventAvro);

        repository.save(sensor);
    }

    @Override
    public HubEventHandlerType getHandlerType() {
        return HubEventHandlerType.DEVICE_ADDED_EVENT;
    }
}
