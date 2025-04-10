package ru.yandex.practicum.avro.mapper;

import com.google.protobuf.Timestamp;

import java.time.Instant;

public class TimestampMapper {
    public static Instant mapToAvro(Timestamp timestampProto) {
        return Instant.ofEpochSecond(
                timestampProto.getSeconds(),
                timestampProto.getNanos()
        );
    }

    public static Timestamp mapToProto(Instant timestampAvro) {
        return Timestamp.newBuilder()
                .setSeconds(timestampAvro.getEpochSecond())
                .setNanos(timestampAvro.getNano())
                .build();
    }
}
