package ru.yandex.practicum.analyzer.event.handler;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.event.model.Scenario;
import ru.yandex.practicum.analyzer.event.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScenarioRemovedEventServiceHandler implements HubEventServiceHandler {
    private final ScenarioRepository scenarioRepository;

    @Override
    @Transactional
    public void handleEvent(HubEventAvro hubEventAvro) {
        ScenarioRemovedEventAvro payload = (ScenarioRemovedEventAvro) hubEventAvro.getPayload();
        String scenarioName = payload.getName();
        String hubId = hubEventAvro.getHubId();

        Scenario scenario = scenarioRepository.findByHubIdAndName(hubId, scenarioName)
                .orElseThrow(() -> new EntityNotFoundException("Scenario not found"));

        Scenario scenarioUpdated = deleteAllScenarioLinks(scenario);
        log.debug("HubEventAvro был переведен в Scenario и будет удалён из БД: {}", scenario);

        scenarioRepository.deleteByHubIdAndName(scenarioUpdated.getHubId(), scenarioUpdated.getName()); // Удаляем сценарий
    }

    private Scenario deleteAllScenarioLinks(Scenario scenario) {
        scenario.getConditions().clear();
        scenario.getActions().clear();

        return scenarioRepository.save(scenario); // Сохраняем изменения в связях
    }

    @Override
    public HubEventServiceType getHandlerType() {
        return HubEventServiceType.SCENARIO_REMOVED_EVENT;
    }
}