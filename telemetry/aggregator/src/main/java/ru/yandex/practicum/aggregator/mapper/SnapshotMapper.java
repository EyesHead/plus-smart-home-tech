package ru.yandex.practicum.aggregator.mapper;


import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

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
