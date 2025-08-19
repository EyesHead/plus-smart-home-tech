package eyeshead.smarthome.analyzer.hubevent.model;

import eyeshead.smarthome.kafka.telemetry.event.DeviceActionAvro;
import eyeshead.smarthome.kafka.telemetry.event.ScenarioConditionAvro;

import java.util.List;

public record ScenarioCreationRequest(
        String hubId,
        String name,
        List<ScenarioConditionAvro> conditions,
        List<DeviceActionAvro> actions
) {}