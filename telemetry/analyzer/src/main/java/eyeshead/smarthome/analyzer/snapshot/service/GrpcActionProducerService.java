package eyeshead.smarthome.analyzer.snapshot.service;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Empty;
import eyeshead.smarthome.grpc.telemetry.event.DeviceActionRequest;
import eyeshead.smarthome.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GrpcActionProducerService {
    @GrpcClient("hub-router")
    HubRouterControllerGrpc.HubRouterControllerFutureStub futureStub;

    /**
     * Асинхронно отправляет DeviceActionRequest через gRPC.
     * Ответ игнорируется, логируется успех или ошибка.
     */
    public void handleDeviceAction(DeviceActionRequest request) {
        ListenableFuture<Empty> future = futureStub.handleDeviceAction(request);

        Futures.addCallback(future, new FutureCallback<>() {
            @Override
            public void onSuccess(Empty result) {
                log.info("gRPC: DeviceActionRequest успешно отправлен: {}", result);
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("gRPC: Ошибка при отправке DeviceActionRequest: {}", t.getMessage(), t);
            }
        }, MoreExecutors.directExecutor());
    }
}