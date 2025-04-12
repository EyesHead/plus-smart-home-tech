package ru.yandex.practicum.avro.mapper;

import ru.yandex.practicum.kafka.telemetry.event.*;

import static ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro.*;

public class SnapshotMapper {
    public static DeviceTypeAvro mapSnapshotToSensorType(Object snapshotData) {
        return switch (snapshotData) {
            case MotionSensorAvro motionSensor -> MOTION_SENSOR;
            case TemperatureSensorAvro temperatureSensor -> TEMPERATURE_SENSOR;
            case LightSensorAvro lightSensor -> LIGHT_SENSOR;
            case ClimateSensorAvro climateSensor -> CLIMATE_SENSOR;
            case SwitchSensorAvro switchSensor -> SWITCH_SENSOR;
            case null, default ->
                    throw new IllegalArgumentException(
                            "Поле data у снапшота невозможно привести к классу, являющимся одним из SensorStateAvro.Data");
        };
    }
}
