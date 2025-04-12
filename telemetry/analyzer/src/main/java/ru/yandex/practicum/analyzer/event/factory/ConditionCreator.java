package ru.yandex.practicum.analyzer.event.factory;

import ru.yandex.practicum.analyzer.event.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

public interface ConditionCreator {
    Condition create(ScenarioConditionAvro conditionAvro);
}
