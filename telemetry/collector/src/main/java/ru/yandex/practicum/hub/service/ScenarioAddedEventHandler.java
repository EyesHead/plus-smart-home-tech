package ru.yandex.practicum.hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.hub.mapper.HubEventMapper;
import ru.yandex.practicum.kafka.HubEventProducerService;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

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
        log.info("Request - ScenarioAddedEvent in proto: {}", hubEventProto);
        HubEventAvro hubEventAvro = HubEventMapper.map(hubEventProto);

        producerService.send(hubEventAvro);
        log.info("ScenarioAddedEvent was send to topic: {}", hubEventAvro);
    }
}