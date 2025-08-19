package ru.yandex.practicum.hubrouter;

import com.google.protobuf.Empty;
import eyeshead.smarthome.grpc.telemetry.event.DeviceActionRequest;
import eyeshead.smarthome.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@Slf4j
public class HubRouterService extends HubRouterControllerGrpc.HubRouterControllerImplBase {
    @Override
    public void handleDeviceAction(DeviceActionRequest request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("GRPC: Получен DeviceActionRequest по grpc: {}", request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}