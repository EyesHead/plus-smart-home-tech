package eyeshead.smarthome.aggregator.service;


import eyeshead.smarthome.kafka.telemetry.event.SensorEventAvro;
import eyeshead.smarthome.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Optional;

public interface SnapshotService {
    Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event);
}