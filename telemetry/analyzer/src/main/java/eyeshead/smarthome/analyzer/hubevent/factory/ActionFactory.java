package eyeshead.smarthome.analyzer.hubevent.factory;

import eyeshead.smarthome.analyzer.hubevent.model.Action;
import eyeshead.smarthome.kafka.telemetry.event.ActionTypeAvro;
import eyeshead.smarthome.kafka.telemetry.event.DeviceActionAvro;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ActionFactory implements ScenarioComponentFactory<Action, DeviceActionAvro> {
    public Action create(DeviceActionAvro actionAvro) {
        validateAction(actionAvro);

        Action action = new Action();
        action.setType(actionAvro.getType());
        action.setValue(actionAvro.getValue());
        return action;
    }

    private void validateAction(DeviceActionAvro action) {
        if (action.getType() == ActionTypeAvro.SET_VALUE && action.getValue() == null) {
            throw new IllegalArgumentException("Для действия с полем type == SET_VALUE value должен быть указан ");
        }
    }
}
