package ru.yandex.practicum.analyzer.hub.factory;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.hub.model.Action;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;

@Component
public class DefaultActionCreator implements ActionCreator {
    @Override
    public Action create(DeviceActionAvro actionAvro) {
        validateAction(actionAvro);

        Action action = new Action();
        action.setType(actionAvro.getType());
        action.setValue(actionAvro.getValue());
        return action;
    }

    private void validateAction(DeviceActionAvro action) {
        if (action.getType() == ActionTypeAvro.SET_VALUE && action.getValue() == null) {
            throw new IllegalArgumentException("SET_VALUE action requires value");
        }
    }
}
