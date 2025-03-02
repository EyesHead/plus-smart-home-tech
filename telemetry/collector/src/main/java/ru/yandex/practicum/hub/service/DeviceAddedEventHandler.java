package ru.yandex.practicum.hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.hub.kafka.HubEventProducerService;
import ru.yandex.practicum.hub.mapper.HubEventMapper;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {
    private final HubEventProducerService producerService;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEventProto hubEventProto) {
        log.info("Request - DeviceAddedEvent in proto: {}", hubEventProto);
        HubEventAvro hubEventAvro = HubEventMapper.map(hubEventProto);

        producerService.send(hubEventAvro);
        log.info("DeviceAddedEvent was send to topic: {}", hubEventAvro);
    }
}
