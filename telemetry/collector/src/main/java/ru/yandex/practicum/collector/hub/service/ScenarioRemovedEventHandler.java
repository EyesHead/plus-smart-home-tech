package ru.yandex.practicum.collector.hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.hub.mapper.HubEventBaseFieldMapper;
import ru.yandex.practicum.collector.kafka.HubEventProducerService;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioRemovedEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioRemovedEventHandler implements HubEventHandler {
    private final HubEventProducerService producerService;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_REMOVED;
    }

    @Override
    public void handle(HubEventProto hubEventProto) {
        log.info("Начинаем обработку ScenarioAddedEvent");

        ScenarioRemovedEventProto protoPayload = hubEventProto.getScenarioRemoved();

        HubEventAvro hubEventAvro = HubEventBaseFieldMapper.map(hubEventProto)
                .setPayload(mapPayload(protoPayload))
                .build();

        producerService.send(hubEventAvro);
        log.info("Данные были переведены в HubEventAvro и отправлены в topic: {}", hubEventAvro);
    }

    private ScenarioRemovedEventAvro mapPayload(ScenarioRemovedEventProto proto) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(proto.getName())
                .build();
    }
}
