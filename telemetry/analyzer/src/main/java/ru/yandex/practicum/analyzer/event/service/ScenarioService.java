package ru.yandex.practicum.analyzer.event.service;

import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.event.factory.ScenarioFactory;
import ru.yandex.practicum.analyzer.event.model.Scenario;
import ru.yandex.practicum.analyzer.event.model.ScenarioCreationRequest;
import ru.yandex.practicum.analyzer.event.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScenarioService {
    private final ScenarioRepository scenarioRepository;
    private final ScenarioFactory scenarioFactory;
    private final ConditionService conditionService;
    private final ActionService actionService;

    @Transactional
    public Scenario createScenario(HubEventAvro hubEvent, ScenarioAddedEventAvro scenarioAvro) {
        if (scenarioRepository.existsByHubIdAndName(hubEvent.getHubId(), scenarioAvro.getName())) {
            log.warn("Scenario already exist: hubId={}, name={}", hubEvent.getHubId(), scenarioAvro.getName());
            throw new EntityExistsException("Scenario already exist");
        }

        Scenario scenario = scenarioFactory.create(new ScenarioCreationRequest(
                hubEvent.getHubId(),
                scenarioAvro.getName(),
                scenarioAvro.getConditions(),
                scenarioAvro.getActions()
        ));

        saveScenarioComponents(scenario);
        return scenarioRepository.save(scenario);
    }

    private void saveScenarioComponents(Scenario scenario) {
        conditionService.saveAll(scenario.getConditions().values());
        actionService.saveAll(scenario.getActions().values());
    }
}