package ru.yandex.practicum.hub.kafka;

import org.apache.avro.io.DatumWriter;
import ru.yandex.practicum.config.BaseAvroSerializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public class HubEventAvroSerializer extends BaseAvroSerializer<HubEventAvro> {
    public HubEventAvroSerializer(DatumWriter<HubEventAvro> writer) {
        super(writer);
    }

    @Override
    public byte[] serialize(String topic, HubEventAvro data) {
        return super.serialize(topic, data);
    }
}