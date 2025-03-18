package ru.yandex.practicum.collector;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.collector.hub.controller.HubEventControllerGrpc;
import ru.yandex.practicum.collector.sensor.controller.SensorEventControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class EventCollector extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final HubEventControllerGrpc hubEventCollector;
    private final SensorEventControllerGrpc sensorEventCollector;

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        hubEventCollector.collectHubEvent(request, responseObserver);
        log.info("----------------------------------------------");
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        sensorEventCollector.collectSensorEvent(request, responseObserver);
        log.info("----------------------------------------------");
    }
}
