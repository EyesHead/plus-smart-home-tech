package eyeshead.smarthome.aggregator.repository;


import eyeshead.smarthome.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Optional;

public interface SnapshotRepository {
    Optional<SensorsSnapshotAvro> getById(String hubId);
    SensorsSnapshotAvro save(SensorsSnapshotAvro sensorEventAvro);
}
