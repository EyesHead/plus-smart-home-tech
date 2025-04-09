package ru.yandex.practicum.collector.hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.hub.mapper.HubEventBaseFieldMapper;
import ru.yandex.practicum.collector.kafka.HubEventProducerService;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
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
        log.info("Начинаем обработку DeviceRemovedEventProto");

        DeviceRemovedEventProto deviceRemovedEventProto = hubEventProto.getDeviceRemoved();

        HubEventAvro hubEventAvro =  HubEventBaseFieldMapper.map(hubEventProto)
                .setPayload(mapPayload(deviceRemovedEventProto))
                        .build();

        producerService.send(hubEventAvro);
        log.info("Данные были переведены в HubEventAvro и отправлены в topic: {}", hubEventAvro);
    }

    private DeviceRemovedEventAvro mapPayload(DeviceRemovedEventProto deviceRemovedEventProto) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(deviceRemovedEventProto.getId())
                .build();
    }
}