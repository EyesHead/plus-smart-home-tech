package ru.yandex.practicum.hub.service;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HubEventAvroSerializer implements Serializer<HubEventAvro> {
    private static final DatumWriter<HubEventAvro> WRITER =
            new SpecificDatumWriter<>(HubEventAvro.class);

    @Override
    public byte[] serialize(String topic, HubEventAvro data) {
        if (data == null) return null;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);

            // Сериализуем весь объект HubEventAvro
            WRITER.write(data, encoder);
            encoder.flush();

            return out.toByteArray();
        } catch (IOException ex) {
            throw new SerializationException(
                    "Ошибка сериализации HubEventAvro для топика [" + topic + "]", ex);
        }

    }
}