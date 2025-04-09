package ru.yandex.practicum.analyzer.hub.handler;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.hub.model.Scenario;
import ru.yandex.practicum.analyzer.hub.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Component
@RequiredArgsConstructor
public class ScenarioRemovedEventServiceHandler implements HubEventServiceHandler {
    private final ScenarioRepository scenarioRepository;

    @Override
    @Transactional
    public void handleEvent(HubEventAvro hubEventAvro) {
        ScenarioRemovedEventAvro payload = (ScenarioRemovedEventAvro) hubEventAvro.getPayload();
        Scenario scenario = scenarioRepository.findByHubIdAndName(hubEventAvro.getHubId(), payload.getName())
                .orElseThrow(() -> new EntityNotFoundException("Scenario not found"));

        // Очищаем связи вручную
        scenario.getConditions().clear();
        scenario.getActions().clear();
        scenarioRepository.save(scenario); // Сохраняем изменения в связях
        scenarioRepository.delete(scenario); // Удаляем сценарий
    }

    @Override
    public HubEventServiceType getHandlerType() {
        return HubEventServiceType.SCENARIO_REMOVED_EVENT;
    }
}