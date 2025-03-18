package ru.yandex.practicum.aggregator.kafka.serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AggregatorKafkaSerializer implements Serializer<SpecificRecordBase> {
    private final EncoderFactory encoderFactory = EncoderFactory.get();

    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) return null;
        final DatumWriter<SpecificRecordBase> WRITER = new SpecificDatumWriter<>(data.getSchema());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = encoderFactory.binaryEncoder(out, null); // Всегда создаём новый encoder
            WRITER.write(data, encoder);
            encoder.flush();  // Гарантируем запись всех данных
            return out.toByteArray();

        } catch (IOException ex) {
            throw new SerializationException("Ошибка сериализации данных для топика [" + topic + "]", ex);
        }
    }
}