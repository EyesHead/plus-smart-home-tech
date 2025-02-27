package ru.yandex.practicum.sensor.kafka;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SensorEventAvroSerializer implements Serializer<SensorEventAvro> {
    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private BinaryEncoder encoder;
    private static final DatumWriter<SensorEventAvro> WRITER =
            new SpecificDatumWriter<>(SensorEventAvro.class);


    @Override
    public byte[] serialize(String topic, SensorEventAvro data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (data == null) return null;

            byte[] result;

            encoder = encoderFactory.binaryEncoder(out, encoder);

            WRITER.write(data, encoder);
            encoder.flush();

            result = out.toByteArray();
            return result;
        } catch (IOException ex) {
            throw new SerializationException("Data serialization error for topic [" + topic + "]", ex);
        }
    }
}