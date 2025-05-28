package ru.yandex.practicum.analyzer.event.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.event.model.Scenario;
import ru.yandex.practicum.analyzer.event.service.ScenarioService;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScenarioAddedEventServiceHandler implements HubEventServiceHandler {
    private final ScenarioService scenarioService;

    @Override
    @Transactional
    public void handleEvent(HubEventAvro hubEventAvro) {
        log.debug("Обработка события добавления сценария: {}", hubEventAvro);
        ScenarioAddedEventAvro scenarioAvro = (ScenarioAddedEventAvro) hubEventAvro.getPayload();

        Scenario scenario = scenarioService.createScenario(hubEventAvro, scenarioAvro);
        log.info("Сценарий создан: {}", scenario);
    }

    @Override
    public HubEventServiceType getHandlerType() {
        return HubEventServiceType.SCENARIO_ADDED_EVENT;
    }
}
