package ru.yandex.practicum.analyzer.snapshot.kafka.deserializer;

import ru.yandex.practicum.avro.serialization.BaseAvroDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

public class SnapshotDeserializer extends BaseAvroDeserializer<SensorsSnapshotAvro> {
    public SnapshotDeserializer() {
        super(SensorsSnapshotAvro.getClassSchema());
    }
}