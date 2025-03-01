package ru.yandex.practicum.sensor.mapper;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public class SensorEventAvroMapper {
    // Схема union для поля payload
    private static final Schema PAYLOAD_UNION_SCHEMA = SensorEventAvro.getClassSchema().getField("payload").schema();

    // Позиции типов в union (порядок как в avro схеме)
    private static final int CLIMATE_SENSOR_POS = 0;
    private static final int LIGHT_SENSOR_POS = 1;
    private static final int MOTION_SENSOR_POS = 2;
    private static final int SWITCH_SENSOR_POS = 3;
    private static final int TEMPERATURE_SENSOR_POS = 4;

    public static SensorEventAvro mapToAvro(SensorEventProto protoSensorEvent,
                                            SensorEventProto.PayloadCase sensorType) {
        return switch (sensorType) {
            case LIGHT_SENSOR_EVENT -> createLightSensorEvent(protoSensorEvent);
            case MOTION_SENSOR_EVENT -> createMotionSensorEvent(protoSensorEvent);
            case TEMPERATURE_SENSOR_EVENT -> createTemperatureSensorEvent(protoSensorEvent);
            case SWITCH_SENSOR_EVENT -> createSwitchSensorEvent(protoSensorEvent);
            case CLIMATE_SENSOR_EVENT -> createClimateSensorEvent(protoSensorEvent);
            case PAYLOAD_NOT_SET -> {
                log.error("Payload for sensor event is missing");
                throw new IllegalArgumentException("Payload for sensor event is required");
            }
        };
    }

    private static SensorEventAvro createClimateSensorEvent(SensorEventProto protoSensorEvent) {
        ClimateSensorEvent protoEvent = protoSensorEvent.getClimateSensorEvent();

        GenericRecord payload = new GenericData.Record(PAYLOAD_UNION_SCHEMA.getTypes().get(CLIMATE_SENSOR_POS));
        payload.put("temperature_c", protoEvent.getTemperatureC());
        payload.put("humidity", protoEvent.getHumidity());
        payload.put("co2_level", protoEvent.getCo2Level());

        return getBaseClassFields(protoSensorEvent)
                .setPayload(payload)
                .build();
    }

    private static SensorEventAvro createSwitchSensorEvent(SensorEventProto protoSensorEvent) {
        SwitchSensorEvent protoEvent = protoSensorEvent.getSwitchSensorEvent();

        GenericRecord payload = new GenericData.Record(PAYLOAD_UNION_SCHEMA.getTypes().get(SWITCH_SENSOR_POS));
        payload.put("state", protoEvent.getState());

        return getBaseClassFields(protoSensorEvent)
                .setPayload(payload)
                .build();
    }

    private static SensorEventAvro createLightSensorEvent(SensorEventProto protoSensorEvent) {
        LightSensorEvent protoEvent = protoSensorEvent.getLightSensorEvent();

        GenericRecord payload = new GenericData.Record(PAYLOAD_UNION_SCHEMA.getTypes().get(LIGHT_SENSOR_POS));
        payload.put("link_quality", protoEvent.getLinkQuality());
        payload.put("luminosity", protoEvent.getLuminosity());

        return getBaseClassFields(protoSensorEvent)
                .setPayload(payload)
                .build();
    }

    private static SensorEventAvro createMotionSensorEvent(SensorEventProto protoSensorEvent) {
        MotionSensorEvent protoEvent = protoSensorEvent.getMotionSensorEvent();

        GenericRecord payload = new GenericData.Record(PAYLOAD_UNION_SCHEMA.getTypes().get(MOTION_SENSOR_POS));
        payload.put("link_quality", protoEvent.getLinkQuality());
        payload.put("motion", protoEvent.getMotion());
        payload.put("voltage", protoEvent.getVoltage());

        return getBaseClassFields(protoSensorEvent)
                .setPayload(payload)
                .build();
    }

    private static SensorEventAvro createTemperatureSensorEvent(SensorEventProto protoSensorEvent) {
        TemperatureSensorEvent protoEvent = protoSensorEvent.getTemperatureSensorEvent();

        GenericRecord payload = new GenericData.Record(PAYLOAD_UNION_SCHEMA.getTypes().get(TEMPERATURE_SENSOR_POS));
        payload.put("temperature_c", protoEvent.getTemperatureC());
        payload.put("temperature_f", protoEvent.getTemperatureF());

        return getBaseClassFields(protoSensorEvent)
                .setPayload(payload)
                .build();
    }

    private static SensorEventAvro.Builder getBaseClassFields(SensorEventProto protoSensorEvent) {
        Instant avroTimestamp = Instant.ofEpochSecond(
                protoSensorEvent.getTimestamp().getSeconds(),
                protoSensorEvent.getTimestamp().getNanos()
        );

        return SensorEventAvro.newBuilder()
                .setId(protoSensorEvent.getId())
                .setHubId(protoSensorEvent.getHubId())
                .setTimestamp(avroTimestamp);
    }
}