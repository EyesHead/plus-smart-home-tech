package eyeshead.smarthome.collector;

import com.google.protobuf.Empty;
import eyeshead.smarthome.collector.hub.controller.HubEventControllerGrpc;
import eyeshead.smarthome.collector.sensor.controller.SensorEventControllerGrpc;
import eyeshead.smarthome.grpc.telemetry.collector.CollectorControllerGrpc;
import eyeshead.smarthome.grpc.telemetry.event.HubEventProto;
import eyeshead.smarthome.grpc.telemetry.event.SensorEventProto;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

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
