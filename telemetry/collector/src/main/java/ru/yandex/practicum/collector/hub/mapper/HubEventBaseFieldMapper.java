package ru.yandex.practicum.collector.hub.mapper;

import ru.yandex.practicum.avro.mapper.TimestampMapper;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public class HubEventBaseFieldMapper {
    public static HubEventAvro.Builder map(HubEventProto proto) {
        return HubEventAvro.newBuilder()
                .setHubId(proto.getHubId())
                .setTimestamp(TimestampMapper.mapToAvro(proto.getTimestamp()));
    }
}