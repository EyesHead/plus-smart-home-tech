package ru.yandex.practicum.sensor.mapper;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Slf4j
public class SensorEventAvroMapper {
    public static SensorEventAvro mapToAvro(SensorEventProto protoSensorEvent,
                                             SensorEventProto.PayloadCase sensorType) {
        return switch (sensorType) {
            case LIGHT_SENSOR_EVENT -> createLightSensorEvent(protoSensorEvent);
            case MOTION_SENSOR_EVENT -> createMotionSensorEvent(protoSensorEvent);
            case TEMPERATURE_SENSOR_EVENT -> createTemperatureSensorEvent(protoSensorEvent);
            case SWITCH_SENSOR_EVENT -> createSwitchSensorEvent(protoSensorEvent);
            case CLIMATE_SENSOR_EVENT -> createClimateSensorEvent(protoSensorEvent);
            case PAYLOAD_NOT_SET -> {
                log.error("Payload for sensor event cant missing");
                throw new IllegalArgumentException("Payload for sensor event is required");
            }
        };
    }

    private static SensorEventAvro createClimateSensorEvent(SensorEventProto protoSensorEvent) {
        SensorEventAvro avroEvent = getBaseClassFields(protoSensorEvent);
        avroEvent.setPayload(protoSensorEvent.getClimateSensorEvent());
        return avroEvent;
    }

    private static SensorEventAvro createSwitchSensorEvent(SensorEventProto protoSensorEvent) {
        SensorEventAvro avroEvent = getBaseClassFields(protoSensorEvent);
        avroEvent.setPayload(protoSensorEvent.getSwitchSensorEvent());
        return avroEvent;
    }

    private static SensorEventAvro createTemperatureSensorEvent(SensorEventProto protoSensorEvent) {
        SensorEventAvro avroEvent = getBaseClassFields(protoSensorEvent);
        avroEvent.setPayload(protoSensorEvent.getTemperatureSensorEvent());
        return avroEvent;
    }

    private static SensorEventAvro createLightSensorEvent(SensorEventProto protoSensorEvent) {
        SensorEventAvro avroEvent = getBaseClassFields(protoSensorEvent);
        avroEvent.setPayload(protoSensorEvent.getLightSensorEvent());
        return avroEvent;
    }

    private static SensorEventAvro createMotionSensorEvent(SensorEventProto protoSensorEvent) {
        SensorEventAvro avroEvent = getBaseClassFields(protoSensorEvent);
        avroEvent.setPayload(protoSensorEvent.getMotionSensorEvent());
        return avroEvent;
    }

    private static SensorEventAvro getBaseClassFields(SensorEventProto protoSensorEvent) {
        SensorEventAvro avroEvent = new SensorEventAvro();
        Timestamp protoTimestamp = protoSensorEvent.getTimestamp();

        avroEvent.setId(protoSensorEvent.getId());
        avroEvent.setHubId(protoSensorEvent.getHubId());
        avroEvent.setTimestamp(Instant.ofEpochSecond(
                        protoTimestamp.getSeconds(),
                        protoTimestamp.getNanos())
                .toEpochMilli());
        return avroEvent;
    }
}
