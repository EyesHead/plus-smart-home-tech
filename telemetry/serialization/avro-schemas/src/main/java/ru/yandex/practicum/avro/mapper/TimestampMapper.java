package ru.yandex.practicum.avro.mapper;

import com.google.protobuf.Timestamp;

import java.time.Instant;

public class TimestampMapper {
    public static Instant mapToInstant(Timestamp timestampProto) {
        return Instant.ofEpochSecond(
                timestampProto.getSeconds(),
                timestampProto.getNanos()
        );
    }
}
