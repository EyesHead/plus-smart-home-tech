package eyeshead.smarthome.collector.hub.service;

import eyeshead.smarthome.grpc.telemetry.event.HubEventProto;

public interface HubEventHandler {
    HubEventProto.PayloadCase getMessageType();

    void handle(HubEventProto request);
}