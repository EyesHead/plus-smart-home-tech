package ru.yandex.practicum.analyzer.hub.factory;

import ru.yandex.practicum.analyzer.hub.model.Action;
import ru.yandex.practicum.analyzer.hub.model.Condition;
import ru.yandex.practicum.analyzer.hub.model.Scenario;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

public class ScenarioFactory {
    public static Scenario createScenario(HubEventAvro hubEventAvro) {
        final Scenario scenario = new Scenario();
        scenario.setHubId(hubEventAvro.getHubId());

        final Object payload = hubEventAvro.getPayload();

        if (payload instanceof ScenarioAddedEventAvro scenarioAddedEvent) {
            scenario.setName(scenarioAddedEvent.getName());

            for (DeviceActionAvro actionAvro : scenarioAddedEvent.getActions()) {
                Action action = createActionFromAvro(actionAvro);
                scenario.getActions().put(actionAvro.getSensorId(), action);
            }

            for (ScenarioConditionAvro conditionAvro : scenarioAddedEvent.getConditions()) {
                Condition condition = createConditionFromAvro(conditionAvro);
                scenario.getConditions().put(conditionAvro.getSensorId(), condition);
            }

            return scenario;
        } else {
            throw new IllegalArgumentException("Невозможно создание сценария не из объекта типа ScenarioAddedEventAvro в payload HubEventAvro");
        }
    }

    private static Condition createConditionFromAvro(ScenarioConditionAvro conditionAvro) {
        Condition condition = new Condition();
        condition.setType(conditionAvro.getType());
        condition.setOperation(conditionAvro.getOperation());
        return condition;
    }

    private static Action createActionFromAvro(DeviceActionAvro actionAvro) {
        Action action = new Action();
        action.setType(actionAvro.getType());
        action.setValue(actionAvro.getValue());
        return action;
    }
}
