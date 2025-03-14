package ru.yandex.practicum.repository;

import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Optional;

public interface SnapshotRepository {
    Optional<SensorsSnapshotAvro> getById(String hubId);
    SensorsSnapshotAvro save(SensorsSnapshotAvro sensorEventAvro);
}
