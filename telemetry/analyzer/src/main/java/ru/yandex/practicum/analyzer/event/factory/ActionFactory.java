package ru.yandex.practicum.analyzer.event.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.event.model.Action;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;

@Component
@RequiredArgsConstructor
public class ActionFactory {
    private final ActionCreator actionCreator;

    public Action create(DeviceActionAvro actionAvro) {
        return actionCreator.create(actionAvro);
    }
}
