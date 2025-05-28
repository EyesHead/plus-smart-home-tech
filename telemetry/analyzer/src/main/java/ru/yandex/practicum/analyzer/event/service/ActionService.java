package ru.yandex.practicum.analyzer.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.event.model.Action;
import ru.yandex.practicum.analyzer.event.repository.ActionRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ActionService {
    private final ActionRepository actionRepository;

    @Transactional
    public void saveAll(Collection<Action> actions) {
        actionRepository.saveAll(actions);
    }
}
