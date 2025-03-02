package ru.yandex.practicum.config;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BaseAvroSerializer<T> implements Serializer<T> {
    protected final EncoderFactory encoderFactory = EncoderFactory.get();
    protected final DatumWriter<T> WRITER;

    public BaseAvroSerializer(DatumWriter<T> writer) {
        WRITER = writer;
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) return null;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = encoderFactory.binaryEncoder(out, null); // Всегда создаём новый encoder
            WRITER.write(data, encoder);
            encoder.flush();  // Гарантируем запись всех данных
            return out.toByteArray();
        } catch (IOException ex) {
            throw new SerializationException("Ошибка сериализации Avro для топика [" + topic + "]", ex);
        }
    }
}
