package ru.yandex.practicum.analyzer.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.event.factory.ConditionFactory;
import ru.yandex.practicum.analyzer.event.model.Condition;
import ru.yandex.practicum.analyzer.event.repository.ConditionRepository;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

@Component
@RequiredArgsConstructor
public class ConditionService {
    private final ConditionRepository conditionRepository;
    private final ConditionFactory conditionFactory;

    @Transactional
    public Condition save(ScenarioConditionAvro conditionAvro) {
        Condition condition = conditionFactory.create(conditionAvro);
        return conditionRepository.save(condition);
    }
}