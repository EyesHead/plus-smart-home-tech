package ru.yandex.practicum.sensor.kafka;

import org.apache.avro.io.DatumWriter;
import ru.yandex.practicum.config.BaseAvroSerializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

public class SensorEventAvroSerializer extends BaseAvroSerializer<SensorEventAvro> {
    public SensorEventAvroSerializer(DatumWriter<SensorEventAvro> writer) {
        super(writer);
    }

    @Override
    public byte[] serialize(String topic, SensorEventAvro data) {
        return super.serialize(topic, data);
    }
}