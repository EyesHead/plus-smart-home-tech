package ru.yandex.collector.hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.collector.hub.mapper.HubEventMapper;
import ru.yandex.collector.kafka.HubEventProducerService;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Slf4j
@RequiredArgsConstructor
@Component
public class DeviceRemovedEventHandler implements HubEventHandler {
    private final HubEventProducerService producerService;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }

    @Override
    public void handle(HubEventProto hubEventProto) {
        log.info("Request - DeviceRemovedEvent in proto: {}", hubEventProto);
        HubEventAvro hubEventAvro = HubEventMapper.map(hubEventProto);

        producerService.send(hubEventAvro);
        log.info("DeviceRemovedEvent was send to topic: {}", hubEventAvro);
    }
}