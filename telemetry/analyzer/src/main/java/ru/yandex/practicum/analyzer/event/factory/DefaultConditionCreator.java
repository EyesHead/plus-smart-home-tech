package ru.yandex.practicum.analyzer.event.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.event.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

@Component
@Slf4j
public class DefaultConditionCreator implements ConditionCreator {
    @Override
    public Condition create(ScenarioConditionAvro conditionAvro) {
        Condition condition = new Condition();
        condition.setType(conditionAvro.getType());
        condition.setOperation(conditionAvro.getOperation());
        condition.setValue(convertValue(conditionAvro.getValue()));
        log.debug("ScenarioConditionAvro был переведен в Condition: {}", condition);
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
            default -> throw new IllegalArgumentException("Unsupported condition value type: " + value.getClass());
        }
    }
}