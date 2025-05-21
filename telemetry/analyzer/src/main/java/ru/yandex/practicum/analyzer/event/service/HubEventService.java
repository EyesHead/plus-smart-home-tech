package ru.yandex.practicum.analyzer.event.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.event.service.handler.HubEventServiceHandler;
import ru.yandex.practicum.analyzer.event.service.handler.HubEventServiceType;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HubEventService {
    private final Map<HubEventServiceType, HubEventServiceHandler> hubEventHandlers;

    @Autowired
    public HubEventService(Set<HubEventServiceHandler> hubEventServices) {
        this.hubEventHandlers = hubEventServices.stream()
                .collect(Collectors.toMap(
                        HubEventServiceHandler::getHandlerType,
                        Function.identity()
                ));
    }

    public void handleRequestEvent(HubEventAvro requestEvent) {
        HubEventServiceType handlerType = getHubEventPayloadType(requestEvent);
        log.info("Начинаю обработку события хаба: {}", handlerType);
        // Обработка события hub
        hubEventHandlers.get(handlerType).handleEvent(requestEvent);
        log.info("------------------------------------------------------");
    }

    private HubEventServiceType getHubEventPayloadType(HubEventAvro hubEventAvro) {
        return switch (hubEventAvro.getPayload()) {
            case DeviceAddedEventAvro ignored -> HubEventServiceType.DEVICE_ADDED_EVENT;
            case DeviceRemovedEventAvro ignored -> HubEventServiceType.DEVICE_REMOVED_EVENT;
            case ScenarioAddedEventAvro ignored -> HubEventServiceType.SCENARIO_ADDED_EVENT;
            case ScenarioRemovedEventAvro ignored -> HubEventServiceType.SCENARIO_REMOVED_EVENT;
            default -> throw new IllegalStateException("Не существующий тип у поля payload объекта HubEventAvro: " + hubEventAvro.getPayload());
        };
    }
}
