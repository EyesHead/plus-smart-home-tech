package ru.yandex.practicum.analyzer.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.event.model.Condition;
import ru.yandex.practicum.analyzer.event.repository.ConditionRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ConditionService {
    private final ConditionRepository conditionRepository;

    @Transactional
    public void saveAll(Collection<Condition> conditions) {
        conditionRepository.saveAll(conditions);
    }
}