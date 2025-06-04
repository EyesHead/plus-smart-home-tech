package eyeshead.smarthome.collector.hub.service;

import eyeshead.smarthome.collector.hub.mapper.HubEventBaseFieldMapper;
import eyeshead.smarthome.collector.kafka.HubEventProducerService;
import eyeshead.smarthome.grpc.telemetry.event.DeviceRemovedEventProto;
import eyeshead.smarthome.grpc.telemetry.event.HubEventProto;
import eyeshead.smarthome.kafka.telemetry.event.DeviceRemovedEventAvro;
import eyeshead.smarthome.kafka.telemetry.event.HubEventAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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