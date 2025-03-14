package ru.yandex.practicum.aggregator.mapper;


import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;

public class SnapshotMapper {
    public static SensorsSnapshotAvro mapToNewSnapshot(SensorEventAvro event) {
        return SensorsSnapshotAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setSensorsState(new HashMap<>())
                .build();
    }

    public static SensorStateAvro mapToState(SensorEventAvro event) {
        return SensorStateAvro.newBuilder()
                .setData(event.getPayload())
                .setTimestamp(event.getTimestamp())
                .build();
    }
}
