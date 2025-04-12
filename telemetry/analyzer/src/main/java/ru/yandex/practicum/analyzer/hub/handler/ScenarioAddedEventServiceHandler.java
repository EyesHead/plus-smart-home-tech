package ru.yandex.practicum.analyzer.hub.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.hub.factory.ScenarioFactory;
import ru.yandex.practicum.analyzer.hub.model.Scenario;
import ru.yandex.practicum.analyzer.hub.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScenarioAddedEventServiceHandler implements HubEventServiceHandler {
    private final ScenarioRepository scenarioRepository;
    private final ScenarioFactory scenarioFactory;

    @Override
    @Transactional
    public void handleEvent(HubEventAvro hubEventAvro) {
        log.debug("Начало сохранения сценария из HubEventAvro в базу данных");
        ScenarioAddedEventAvro scenarioAvro = (ScenarioAddedEventAvro) hubEventAvro.getPayload();

        if (scenarioRepository.existsByHubIdAndName(hubEventAvro.getHubId(), scenarioAvro.getName())) {
            log.warn("Сценарий с name = {} и hubId = {} уже существует в базе данных и не будет сохранён",
                    scenarioAvro.getName(), hubEventAvro.getHubId());
            return;
        }

        Scenario scenario = scenarioFactory.createScenario(hubEventAvro, scenarioAvro);

        log.info("HubEventAvro был переведен в Scenario и будет сохранён в БД: {}", scenario);

        Scenario saved = scenarioRepository.save(scenario);
        log.debug("Сценарий сохранен: {}", saved);
    }

    @Override
    public HubEventServiceType getHandlerType() {
        return HubEventServiceType.SCENARIO_ADDED_EVENT;
    }
}
