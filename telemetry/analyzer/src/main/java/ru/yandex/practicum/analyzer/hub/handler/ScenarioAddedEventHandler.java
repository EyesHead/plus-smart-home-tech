package ru.yandex.practicum.analyzer.hub.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.hub.factory.ScenarioFactory;
import ru.yandex.practicum.analyzer.hub.model.Scenario;
import ru.yandex.practicum.analyzer.hub.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {
    private final ScenarioRepository scenarioRepository;

    @Override
    public void handle(HubEventAvro hubEventAvro) {
        Scenario scenario = ScenarioFactory.createScenario(hubEventAvro);
        scenarioRepository.save(scenario);
    }

    @Override
    public HubEventHandlerType getHandlerType() {
        return HubEventHandlerType.SCENARIO_ADDED_EVENT;
    }
}
