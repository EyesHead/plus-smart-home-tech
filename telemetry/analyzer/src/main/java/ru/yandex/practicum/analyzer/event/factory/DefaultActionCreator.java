package ru.yandex.practicum.analyzer.event.factory;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.event.model.Action;
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
            throw new IllegalArgumentException("Для значения поля SET_VALUE у действия должно быть поле value");
        }
    }
}
