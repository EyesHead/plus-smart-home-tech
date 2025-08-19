package eyeshead.smarthome.aggregator.service;


import eyeshead.smarthome.kafka.telemetry.event.SensorEventAvro;
import eyeshead.smarthome.kafka.telemetry.event.SensorStateAvro;
import eyeshead.smarthome.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Map;

public class SnapshotMapper {
    public static SensorsSnapshotAvro mapToNewSnapshot(SensorEventAvro sensor) {
        return SensorsSnapshotAvro.newBuilder()
                .setHubId(sensor.getHubId())
                .setTimestamp(sensor.getTimestamp())
                .setSensorsState(Map.of(sensor.getId(), mapToState(sensor)))
                .build();
    }

    public static SensorStateAvro mapToState(SensorEventAvro event) {
        return SensorStateAvro.newBuilder()
                .setData(event.getPayload())
                .setTimestamp(event.getTimestamp())
                .build();
    }
}
