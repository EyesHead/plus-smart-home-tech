package ru.yandex.practicum.analyzer.hub.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.hub.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Component
@RequiredArgsConstructor
public class ScenarioRemovedEventHandler implements HubEventHandler {
    private final ScenarioRepository repository;

    @Override
    public void handle(HubEventAvro hubEventAvro) {
        ScenarioRemovedEventAvro eventExtracted = (ScenarioRemovedEventAvro) hubEventAvro.getPayload();
        repository.deleteByName(eventExtracted.getName());
    }

    @Override
    public HubEventHandlerType getHandlerType() {
        return HubEventHandlerType.SCENARIO_REMOVED_EVENT;
    }
}