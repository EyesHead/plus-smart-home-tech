package ru.yandex.practicum.analyzer.snapshot.kafka.deserializer;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.serialization.BaseAvroDeserializer;

public class SnapshotDeserializer extends BaseAvroDeserializer<SensorEventAvro> {
    public SnapshotDeserializer() {
        super(SensorsSnapshotAvro.getClassSchema());
    }
}