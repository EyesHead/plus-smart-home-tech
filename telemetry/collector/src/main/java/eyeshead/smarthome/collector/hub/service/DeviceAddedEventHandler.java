package eyeshead.smarthome.collector.hub.service;

import eyeshead.smarthome.collector.hub.mapper.HubEventBaseFieldMapper;
import eyeshead.smarthome.collector.kafka.HubEventProducerService;
import eyeshead.smarthome.grpc.telemetry.event.DeviceAddedEventProto;
import eyeshead.smarthome.grpc.telemetry.event.HubEventProto;
import eyeshead.smarthome.kafka.telemetry.event.DeviceAddedEventAvro;
import eyeshead.smarthome.kafka.telemetry.event.DeviceTypeAvro;
import eyeshead.smarthome.kafka.telemetry.event.HubEventAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
