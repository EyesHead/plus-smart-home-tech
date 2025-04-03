package ru.yandex.practicum.analyzer.hub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.hub.handler.HubEventHandler;
import ru.yandex.practicum.analyzer.hub.handler.HubEventHandlerType;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HubEventService {
    private final Map<HubEventHandlerType, HubEventHandler> hubEventHandlers;

    @Autowired
    public HubEventService(Set<HubEventHandler> hubEventHandlers) {
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getHandlerType,
                        Function.identity()
                ));
    }

    public void handleRequest(HubEventAvro request) {
        HubEventHandlerType handlerType = getHubEventPayloadType(request);

        // Обработка события hub
        hubEventHandlers.get(handlerType).handle(request);
    }

    private HubEventHandlerType getHubEventPayloadType(HubEventAvro hubEventAvro) {
        return switch (hubEventAvro.getPayload()) {
            case DeviceAddedEventAvro ignored -> HubEventHandlerType.DEVICE_ADDED_EVENT;
            case DeviceRemovedEventAvro ignored -> HubEventHandlerType.DEVICE_REMOVED_EVENT;
            case ScenarioAddedEventAvro ignored -> HubEventHandlerType.SCENARIO_ADDED_EVENT;
            case ScenarioRemovedEventAvro ignored -> HubEventHandlerType.SCENARIO_REMOVED_EVENT;
            default -> throw new IllegalStateException("Не существующий тип у поля payload объекта HubEventAvro: " + hubEventAvro.getPayload());
        };
    }
}
