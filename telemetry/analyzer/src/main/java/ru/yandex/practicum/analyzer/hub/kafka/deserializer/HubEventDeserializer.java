package ru.yandex.practicum.analyzer.hub.kafka.deserializer;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.serialization.BaseAvroDeserializer;

public class HubEventDeserializer extends BaseAvroDeserializer<HubEventAvro> {
    public HubEventDeserializer() {
        super(HubEventAvro.getClassSchema());
    }
}