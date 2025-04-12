package ru.yandex.practicum.analyzer.event.kafka.deserializer;

import ru.yandex.practicum.avro.serialization.BaseAvroDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public class HubEventDeserializer extends BaseAvroDeserializer<HubEventAvro> {
    public HubEventDeserializer() {
        super(HubEventAvro.getClassSchema());
    }
}