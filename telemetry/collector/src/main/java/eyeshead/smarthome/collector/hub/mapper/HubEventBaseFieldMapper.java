package eyeshead.smarthome.collector.hub.mapper;

import eyes.head.smarthome.avro.mapper.TimestampMapper;
import eyeshead.smarthome.grpc.telemetry.event.HubEventProto;
import eyeshead.smarthome.kafka.telemetry.event.HubEventAvro;

public class HubEventBaseFieldMapper {
    public static HubEventAvro.Builder map(HubEventProto proto) {
        return HubEventAvro.newBuilder()
                .setHubId(proto.getHubId())
                .setTimestamp(TimestampMapper.mapToAvro(proto.getTimestamp()));
    }
}