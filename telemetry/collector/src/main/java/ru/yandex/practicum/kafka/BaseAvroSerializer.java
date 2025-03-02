package ru.yandex.practicum.kafka;

import org.apache.avro.io.DatumWriter;
<<<<<<<< HEAD:telemetry/collector/src/main/java/ru/yandex/practicum/hub/kafka/HubEventAvroSerializer.java
import ru.yandex.practicum.config.BaseAvroSerializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public class HubEventAvroSerializer extends BaseAvroSerializer<HubEventAvro> {
    public HubEventAvroSerializer(DatumWriter<HubEventAvro> writer) {
        super(writer);
    }

    @Override
    public byte[] serialize(String topic, HubEventAvro data) {
        return super.serialize(topic, data);
========
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BaseAvroSerializer implements Serializer<SpecificRecordBase> {
    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private BinaryEncoder encoder;

    public byte[] serialize(String topic, SpecificRecordBase data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] result = null;
            encoder = encoderFactory.binaryEncoder(out, encoder);
            if (data != null) {
                DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
                writer.write(data, encoder);
                encoder.flush();
                result = out.toByteArray();
            }
            return result;
        } catch (IOException ex) {
            throw new SerializationException("Ошибка сериализации данных для топика [" + topic + "]", ex);
        }
>>>>>>>> ae7f751 (fixed: base avro serializer initialized):telemetry/collector/src/main/java/ru/yandex/practicum/kafka/BaseAvroSerializer.java
    }
}