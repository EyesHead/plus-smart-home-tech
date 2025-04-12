package ru.yandex.practicum.analyzer.snapshot.service;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Component
@Slf4j
@RequiredArgsConstructor
public class ActionProducerService {
    @GrpcClient("hub-router")
    HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterController;

    public void handleDeviceAction(DeviceActionRequest deviceAction) {
        try {
            // отправка сообщения с помощью готового к использованию метода
            hubRouterController.handleDeviceAction(deviceAction);
            log.info("gRPC: DeviceActionRequest был отправлен из analyzer");
        } catch (StatusRuntimeException e) {
            log.error("gRPC: Ошибка отправки сообщения из analyzer: {}", e.getStatus(), e);
        }
    }
}