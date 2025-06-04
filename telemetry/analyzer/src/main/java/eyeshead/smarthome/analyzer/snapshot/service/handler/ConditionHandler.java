package eyeshead.smarthome.analyzer.snapshot.service.handler;

import eyeshead.smarthome.analyzer.hubevent.model.Condition;
import eyeshead.smarthome.kafka.telemetry.event.DeviceTypeAvro;
import eyeshead.smarthome.kafka.telemetry.event.SensorStateAvro;

public interface ConditionHandler {
    boolean isTriggered(Condition condition, SensorStateAvro sensorData);

    DeviceTypeAvro getDeviceType();
}