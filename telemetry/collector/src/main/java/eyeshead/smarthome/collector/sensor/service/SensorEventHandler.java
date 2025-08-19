package eyeshead.smarthome.collector.sensor.service;

import eyeshead.smarthome.grpc.telemetry.event.SensorEventProto;

public interface SensorEventHandler {
    SensorEventProto.PayloadCase getMessageType();

    void handle(SensorEventProto sensorEventProto);
}