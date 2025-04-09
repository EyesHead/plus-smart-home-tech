package ru.yandex.practicum.analyzer.hub.factory;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.hub.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

@Component("defaultConditionCreator")
public class DefaultConditionCreator implements ConditionCreator {
    @Override
    public Condition create(ScenarioConditionAvro conditionAvro) {
        Condition condition = new Condition();
        condition.setType(conditionAvro.getType());
        condition.setOperation(conditionAvro.getOperation());
        condition.setValue(convertValue(conditionAvro.getValue()));
        return condition;
    }

    private Integer convertValue(Object value) {
        switch (value) {
            case null -> {
                return null;
            }
            case Boolean b -> {
                return b ? 1 : 0;
            }
            case Integer i -> {
                return i;
            }
            case Number number -> {
                return number.intValue();
            }
            case String s -> {
                if (s.equals("true")) {
                    return 1;
                } else if (s.equals("false")) {
                    return 0;
                } else {
                    return null;
                }
            }
            default -> throw new IllegalArgumentException("Unsupported condition value type: " + value.getClass());
        }
    }
}