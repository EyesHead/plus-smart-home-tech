package ru.yandex.practicum.analyzer.event.model;

import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

import java.util.List;

public record ScenarioCreationRequest(
        String hubId,
        String name,
        List<ScenarioConditionAvro> conditions,
        List<DeviceActionAvro> actions
) {}