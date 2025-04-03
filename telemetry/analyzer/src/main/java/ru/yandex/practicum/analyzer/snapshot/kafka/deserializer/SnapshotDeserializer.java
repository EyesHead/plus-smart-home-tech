package ru.yandex.practicum.analyzer.snapshot.kafka.deserializer;

import ru.yandex.practicum.aggregator.kafka.deserializer.BaseAvroDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

public class SnapshotDeserializer extends BaseAvroDeserializer<SensorEventAvro> {
    public SnapshotDeserializer() {
        super(SensorsSnapshotAvro.getClassSchema());
    }
}