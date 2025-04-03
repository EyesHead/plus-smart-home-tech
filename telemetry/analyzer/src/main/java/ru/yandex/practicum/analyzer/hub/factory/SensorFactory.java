package ru.yandex.practicum.analyzer.hub.factory;

import ru.yandex.practicum.analyzer.hub.model.Sensor;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public class SensorFactory {
    public static Sensor createSensor(HubEventAvro hubEventAvro) {
        final Sensor sensor = new Sensor();
        sensor.setHubId(hubEventAvro.getHubId());

        final Object deviceAddedEvent = hubEventAvro.getPayload();

        if (deviceAddedEvent instanceof DeviceAddedEventAvro) {
            sensor.setId(((DeviceAddedEventAvro) deviceAddedEvent).getId());
            sensor.setType(((DeviceAddedEventAvro) deviceAddedEvent).getType());
            return sensor;
        } else {
            throw new IllegalArgumentException("Невозможно создание сенсора не из объекта типа DeviceAddedEventAvro в payload HubEventAvro");
        }
    }
}
