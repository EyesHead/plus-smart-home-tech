package eyeshead.smarthome.analyzer.hubevent.service.handler;

import eyeshead.smarthome.analyzer.hubevent.model.Scenario;
import eyeshead.smarthome.analyzer.hubevent.service.ScenarioService;
import eyeshead.smarthome.kafka.telemetry.event.HubEventAvro;
import eyeshead.smarthome.kafka.telemetry.event.ScenarioAddedEventAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
