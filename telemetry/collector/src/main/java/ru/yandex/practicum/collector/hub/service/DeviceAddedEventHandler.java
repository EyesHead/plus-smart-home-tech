package ru.yandex.practicum.collector.hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.hub.mapper.HubEventBaseFieldMapper;
import ru.yandex.practicum.collector.kafka.HubEventProducerService;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
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
        log.info("Начинаем обработку DeviceAddedEventProto");

        DeviceAddedEventProto deviceAddedEventProto = hubEventProto.getDeviceAdded();

        HubEventAvro hubEventAvro = HubEventBaseFieldMapper.map(hubEventProto)
                .setPayload(mapPayload(deviceAddedEventProto))
                .build();

        producerService.send(hubEventAvro);
        log.info("Данные были переведены в HubEventAvro и отправлены в topic: {}", hubEventAvro);
    }

    private DeviceAddedEventAvro mapPayload(DeviceAddedEventProto hubEventProto) {
        return DeviceAddedEventAvro.newBuilder()
                .setId(hubEventProto.getId())
                .setType(DeviceTypeAvro.valueOf(hubEventProto.getType().name()))
                .build();
    }
}
