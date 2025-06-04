package eyeshead.smarthome.analyzer.hubevent.service.handler;

import eyeshead.smarthome.kafka.telemetry.event.HubEventAvro;

public interface HubEventServiceHandler {
    void handleEvent(HubEventAvro hubEventAvro);

    HubEventServiceType getHandlerType();
}
