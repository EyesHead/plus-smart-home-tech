package ru.yandex.practicum.config;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.hub.controller.HubEventControllerGrpc;
import ru.yandex.practicum.sensor.controller.SensorEventControllerGrpc;

@GrpcService
@RequiredArgsConstructor
public class EventCollector extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final HubEventControllerGrpc hubEventCollector;
    private final SensorEventControllerGrpc sensorEventCollector;

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        hubEventCollector.collectHubEvent(request, responseObserver);
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        sensorEventCollector.collectSensorEvent(request, responseObserver);
    }
}
