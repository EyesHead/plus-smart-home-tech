package ru.yandex.practicum.aggregator.kafka.deserializer;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.serialization.BaseAvroDeserializer;

public class SensorEventDeserializer extends BaseAvroDeserializer<SensorEventAvro> {
    public SensorEventDeserializer() {
        super(SensorEventAvro.getClassSchema());
    }
}
