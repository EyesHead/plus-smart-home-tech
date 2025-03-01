package ru.yandex.practicum.hub.mapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HubEventAvroMapper {
    // Схема union для поля payload
    private static final Schema PAYLOAD_UNION_SCHEMA = HubEventAvro.getClassSchema().getField("payload").schema();

    // Позиции типов в union (порядок как в avro схеме)
    private static final int DEVICE_ADDED_POS = 0;
    private static final int DEVICE_REMOVED_POS = 1;
    private static final int SCENARIO_ADDED_POS = 2;
    private static final int SCENARIO_REMOVED_POS = 3;

    public static HubEventAvro mapToAvro(HubEventProto protoEvent) {
        HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                .setHubId(protoEvent.getHubId())
                .setTimestamp(convertTimestamp(protoEvent.getTimestamp()));

        switch (protoEvent.getPayloadCase()) {
            case DEVICE_ADDED:
                builder.setPayload(convertDeviceAdded(protoEvent.getDeviceAdded()));
                break;
            case DEVICE_REMOVED:
                builder.setPayload(convertDeviceRemoved(protoEvent.getDeviceRemoved()));
                break;
            case SCENARIO_ADDED:
                builder.setPayload(convertScenarioAdded(protoEvent.getScenarioAdded()));
                break;
            case SCENARIO_REMOVED:
                builder.setPayload(convertScenarioRemoved(protoEvent.getScenarioRemoved()));
                break;
            case PAYLOAD_NOT_SET:
                log.error("Hub event payload is missing");
                throw new IllegalArgumentException("Hub event payload is required");
        }

        return builder.build();
    }

    private static GenericRecord convertDeviceAdded(DeviceAddedEventProto proto) {
        GenericRecord record = new GenericData.Record(PAYLOAD_UNION_SCHEMA.getTypes().get(DEVICE_ADDED_POS));
        record.put("id", proto.getId());
        record.put("type", DeviceTypeAvro.values()[proto.getType().ordinal()]);
        return record;
    }

    private static GenericRecord convertDeviceRemoved(DeviceRemovedEventProto proto) {
        GenericRecord record = new GenericData.Record(PAYLOAD_UNION_SCHEMA.getTypes().get(DEVICE_REMOVED_POS));
        record.put("id", proto.getId());
        return record;
    }

    private static GenericRecord convertScenarioAdded(ScenarioAddedEventProto proto) {
        GenericRecord record = new GenericData.Record(PAYLOAD_UNION_SCHEMA.getTypes().get(SCENARIO_ADDED_POS));

        record.put("name", proto.getName());
        record.put("conditions", convertConditions(proto.getConditionList()));
        record.put("actions", convertActions(proto.getActionList()));

        return record;
    }

    private static GenericRecord convertScenarioRemoved(ScenarioRemovedEventProto proto) {
        GenericRecord record = new GenericData.Record(PAYLOAD_UNION_SCHEMA.getTypes().get(SCENARIO_REMOVED_POS));
        record.put("name", proto.getName());
        return record;
    }

    private static List<GenericRecord> convertConditions(List<ScenarioConditionProto> conditions) {
        return conditions.stream().map(condition -> {
            GenericRecord record = new GenericData.Record(ScenarioConditionAvro.getClassSchema());

            record.put("sensor_id", condition.getSensorId());
            record.put("type", ConditionTypeAvro.values()[condition.getType().ordinal()]);
            record.put("operation", ConditionOperationAvro.values()[condition.getOperation().ordinal()]);

            // Обработка union значения
            if (condition.hasBoolValue()) {
                record.put("value", condition.getBoolValue());
            } else if (condition.hasIntValue()) {
                record.put("value", condition.getIntValue());
            } else {
                record.put("value", null);
            }

            return record;
        }).collect(Collectors.toList());
    }

    private static List<GenericRecord> convertActions(List<DeviceActionProto> actions) {
        return actions.stream().map(action -> {
            GenericRecord record = new GenericData.Record(DeviceActionAvro.getClassSchema());

            record.put("sensor_id", action.getSensorId());
            record.put("type", ActionTypeAvro.values()[action.getType().ordinal()]);

            if (action.hasValue()) {
                record.put("value", action.getValue());
            } else {
                record.put("value", null);
            }

            return record;
        }).collect(Collectors.toList());
    }

    private static Instant convertTimestamp(com.google.protobuf.Timestamp timestamp) {
        return Instant.ofEpochSecond(
                timestamp.getSeconds(),
                timestamp.getNanos()
        );
    }
}