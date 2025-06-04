package eyeshead.smarthome.analyzer.hubevent.factory;

import eyeshead.smarthome.analyzer.hubevent.model.Condition;
import eyeshead.smarthome.kafka.telemetry.event.ScenarioConditionAvro;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
