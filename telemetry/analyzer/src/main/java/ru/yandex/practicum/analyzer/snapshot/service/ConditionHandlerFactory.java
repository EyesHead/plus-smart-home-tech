package ru.yandex.practicum.analyzer.snapshot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConditionHandlerFactory {
    private final Map<DeviceTypeAvro, ConditionHandler> handlers;

    @Autowired
    public ConditionHandlerFactory(Set<ConditionHandler> conditionHandlers) {
        this.handlers = conditionHandlers.stream()
                .collect(Collectors.toMap(
                        ConditionHandler::getDeviceType,
                        Function.identity()
                ));
    }

    public ConditionHandler getHandler(DeviceTypeAvro type) {
        return handlers.get(type);
    }
}
