package ru.yandex.practicum.analyzer.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.event.factory.ActionFactory;
import ru.yandex.practicum.analyzer.event.model.Action;
import ru.yandex.practicum.analyzer.event.repository.ActionRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;

@Component
@RequiredArgsConstructor
public class ActionService {
    private final ActionRepository actionRepository;
    private final ActionFactory actionFactory;

    @Transactional
    public Action save(DeviceActionAvro actionAvro) {
        Action action = actionFactory.create(actionAvro);
        return actionRepository.save(action);
    }
}
