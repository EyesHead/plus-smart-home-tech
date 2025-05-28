package ru.yandex.practicum.analyzer.event.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.event.model.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

@Component
@Slf4j
public class ConditionFactory implements ScenarioComponentFactory<Condition, ScenarioConditionAvro> {
    @Override
    public Condition create(ScenarioConditionAvro conditionAvro) {
        Condition condition = new Condition();
        condition.setType(conditionAvro.getType());
        condition.setOperation(conditionAvro.getOperation());
        condition.setValue(convertValue(conditionAvro.getValue()));
        log.debug("Condition создан: {}", condition);
        return condition;
    }

    /**
     * Метод преобразует значение из protobuf union в integer:
     * true -> 1, false -> 0, иначе сохраняет как есть.
     */
    private Integer convertValue(Object value) {
        if (!(value instanceof Boolean)) {
            return (Integer) value;
        }
        return (Boolean) value ? 1 : 0;
    }
}
