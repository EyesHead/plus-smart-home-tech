package ru.yandex.practicum.analyzer.hub.factory;

import ru.yandex.practicum.analyzer.hub.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

public interface ConditionCreator {
    Condition create(ScenarioConditionAvro conditionAvro);
}
