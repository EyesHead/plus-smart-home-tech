package ru.yandex.practicum.collector.sensor.mapper;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

public class SensorEventMapper {
    public static SensorEventAvro map(SensorEventProto proto) {
        return SensorEventAvro.newBuilder()
                .setId(proto.getId())
                .setHubId(proto.getHubId())
                .setTimestamp(Instant.ofEpochSecond(proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos()))
                .setPayload(mapPayload(proto))
                .build();
    }

    private static Object mapPayload(SensorEventProto proto) {
        return switch (proto.getPayloadCase()) {
            case MOTION_SENSOR_EVENT -> MotionSensorAvro.newBuilder()
                    .setLinkQuality(proto.getMotionSensorEvent().getLinkQuality())
                    .setMotion(proto.getMotionSensorEvent().getMotion())
                    .setVoltage(proto.getMotionSensorEvent().getVoltage())
                    .build();
            case TEMPERATURE_SENSOR_EVENT -> TemperatureSensorAvro.newBuilder()
                    .setTemperatureC(proto.getTemperatureSensorEvent().getTemperatureC())
                    .setTemperatureF(proto.getTemperatureSensorEvent().getTemperatureF())
                    .build();
            case LIGHT_SENSOR_EVENT -> LightSensorAvro.newBuilder()
                    .setLinkQuality(proto.getLightSensorEvent().getLinkQuality())
                    .setLuminosity(proto.getLightSensorEvent().getLuminosity())
                    .build();
            case CLIMATE_SENSOR_EVENT -> ClimateSensorAvro.newBuilder()
                    .setTemperatureC(proto.getClimateSensorEvent().getTemperatureC())
                    .setHumidity(proto.getClimateSensorEvent().getHumidity())
                    .setCo2Level(proto.getClimateSensorEvent().getCo2Level())
                    .build();
            case SWITCH_SENSOR_EVENT -> SwitchSensorAvro.newBuilder()
                    .setState(proto.getSwitchSensorEvent().getState())
                    .build();
            default -> throw new IllegalArgumentException("Unknown sensor event type");
        };
    }
}
