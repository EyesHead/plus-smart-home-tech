package ru.yandex.practicum.collector.hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.hub.mapper.HubEventBaseFieldMapper;
import ru.yandex.practicum.collector.kafka.HubEventProducerService;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {
    private final HubEventProducerService producerService;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public void handle(HubEventProto hubEventProto) {
        log.info("Начинаем обработку ScenarioAddedEvent");

        ScenarioAddedEventProto scenarioAddedProto = hubEventProto.getScenarioAdded();

        HubEventAvro hubEventAvro = HubEventBaseFieldMapper.map(hubEventProto)
                .setPayload(mapPayload(scenarioAddedProto))
                .build();

        producerService.send(hubEventAvro);
        log.info("Данные были переведены в HubEventAvro и отправлены в topic: {}", hubEventAvro);
    }

    private ScenarioAddedEventAvro mapPayload(ScenarioAddedEventProto scenarioAddedProto) {
        return ScenarioAddedEventAvro.newBuilder()
                .setName(scenarioAddedProto.getName())
                .setActions(
                        scenarioAddedProto.getActionList()
                                .stream()
                                .map(this::mapAction)
                                .collect(Collectors.toList())
                )
                .setConditions(
                        scenarioAddedProto.getConditionList()
                                .stream()
                                .map(this::mapCondition)
                                .collect(Collectors.toList())
                )
                .build();
    }

    private ScenarioConditionAvro mapCondition(ScenarioConditionProto conditionProto) {
        return ScenarioConditionAvro.newBuilder()
                .setType(ConditionTypeAvro.valueOf(conditionProto.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(conditionProto.getOperation().name()))
                .setValue(conditionProto.hasBoolValue()
                        ? Integer.valueOf(conditionProto.getBoolValue() ? 1 : 0)
                        : conditionProto.hasIntValue() ? conditionProto.getIntValue()
                        : null
                )
                .setSensorId(conditionProto.getSensorId())
                .build();
    }

    private DeviceActionAvro mapAction(DeviceActionProto deviceActionProto) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(deviceActionProto.getSensorId())
                .setType(ActionTypeAvro.valueOf(deviceActionProto.getType().name()))
                .setValue(deviceActionProto.hasValue() ? deviceActionProto.getValue() : null)
                .build();
    }
}