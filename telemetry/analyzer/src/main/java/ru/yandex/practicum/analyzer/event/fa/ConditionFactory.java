package ru.yandex.practicum.analyzer.event.fa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.event.factory.ConditionCreator;
import ru.yandex.practicum.analyzer.event.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

@Component
@RequiredArgsConstructor
public class ConditionFactory {
    private final ConditionCreator conditionCreator;

    public Condition create(ScenarioConditionAvro conditionAvro) {
        return conditionCreator.create(conditionAvro);
    }
}
