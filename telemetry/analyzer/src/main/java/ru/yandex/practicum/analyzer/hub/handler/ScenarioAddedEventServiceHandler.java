package ru.yandex.practicum.analyzer.hub.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.hub.factory.ScenarioFactory;
import ru.yandex.practicum.analyzer.hub.model.Scenario;
import ru.yandex.practicum.analyzer.hub.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Component
@RequiredArgsConstructor
public class ScenarioAddedEventServiceHandler implements HubEventServiceHandler {
    private final ScenarioRepository scenarioRepository;
    private final ScenarioFactory scenarioFactory;

    @Override
    @Transactional
    public void handleEvent(HubEventAvro hubEventAvro) {
        Scenario scenario = scenarioFactory.createScenario(hubEventAvro);

        // Проверка на существующий сценарий
        if (scenarioRepository.existsByHubIdAndName(scenario.getHubId(), scenario.getName())) {
            throw new IllegalStateException("Scenario already exists");
        }

        scenarioRepository.save(scenario);    }

    @Override
    public HubEventServiceType getHandlerType() {
        return HubEventServiceType.SCENARIO_ADDED_EVENT;
    }
}
