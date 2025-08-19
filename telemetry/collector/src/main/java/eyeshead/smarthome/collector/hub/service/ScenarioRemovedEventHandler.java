package eyeshead.smarthome.collector.hub.service;

import eyeshead.smarthome.collector.hub.mapper.HubEventBaseFieldMapper;
import eyeshead.smarthome.collector.kafka.HubEventProducerService;
import eyeshead.smarthome.grpc.telemetry.event.HubEventProto;
import eyeshead.smarthome.grpc.telemetry.event.ScenarioRemovedEventProto;
import eyeshead.smarthome.kafka.telemetry.event.HubEventAvro;
import eyeshead.smarthome.kafka.telemetry.event.ScenarioRemovedEventAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
