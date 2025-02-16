package ru.yandex.practicum.sensor.mapper;

import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.sensor.model.*;
import ru.yandex.practicum.sensor.model.SensorEvent;

public class SensorEventAvroMapper {

    public static ru.yandex.practicum.kafka.telemetry.event.SensorEvent toAvro(SensorEvent event) {
        var avroEvent = new ru.yandex.practicum.kafka.telemetry.event.SensorEvent();
        avroEvent.setId(event.getId());
        avroEvent.setHubId(event.getHubId());
        avroEvent.setTimestamp(event.getTimestamp().toEpochMilli());

        switch (event.getType()) {
            case CLIMATE_SENSOR_EVENT -> {
                ClimateSensorEvent climateEvent = (ClimateSensorEvent) event;
                avroEvent.setPayload(createClimateSensor(climateEvent));
            }
            case TEMPERATURE_SENSOR_EVENT -> {
                TemperatureSensorEvent tempEvent = (TemperatureSensorEvent) event;
                avroEvent.setPayload(createTemperatureSensor(tempEvent));
            }
            case MOTION_SENSOR_EVENT -> {
                MotionSensorEvent motionEvent = (MotionSensorEvent) event;
                avroEvent.setPayload(createMotionSensor(motionEvent));
            }
            case LIGHT_SENSOR_EVENT -> {
                LightSensorEvent lightEvent = (LightSensorEvent) event;
                avroEvent.setPayload(createLightSensor(lightEvent));
            }
            case SWITCH_SENSOR_EVENT -> {
                SwitchSensorEvent switchEvent = (SwitchSensorEvent) event;
                avroEvent.setPayload(createSwitchSensor(switchEvent));
            }
            default -> throw new IllegalArgumentException("Unsupported event type: " + event.getType());
        }

        return avroEvent;
    }

    private static ClimateSensor createClimateSensor(ClimateSensorEvent event) {
        return ClimateSensor.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setHumidity(event.getHumidity())
                .setCo2Level(event.getCo2Level())
                .build();
    }

    private static TemperatureSensor createTemperatureSensor(TemperatureSensorEvent event) {
        return TemperatureSensor.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setTemperatureF(event.getTemperatureF())
                .build();
    }

    private static MotionSensor createMotionSensor(MotionSensorEvent event) {
        return MotionSensor.newBuilder()
                .setMotion(event.isMotion())
                .setLinkQuality(event.getLinkQuality())
                .setVoltage(event.getVoltage())
                .build();
    }

    private static SwitchSensor createSwitchSensor(SwitchSensorEvent event) {
        return SwitchSensor.newBuilder()
                .setState(event.isState())
                .build();
    }

    private static LightSensor createLightSensor(LightSensorEvent event) {
        return LightSensor.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setLuminosity(event.getLuminosity())
                .build();
    }
}