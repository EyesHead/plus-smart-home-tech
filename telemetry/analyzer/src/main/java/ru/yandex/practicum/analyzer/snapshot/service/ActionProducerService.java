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
    @GrpcClient("hub-router") // внедрение с параметром "echo" — это отсылка к блоку конфигурации
    HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterController;

    public void handleDeviceAction(DeviceActionRequest deviceAction) {
        try {
            // отправка сообщения с помощью готового к использованию метода
            hubRouterController.handleDeviceAction(deviceAction);
            log.info("Action was send");
        } catch (StatusRuntimeException e) {
            log.error("Failed to send action: {}", e.getStatus(), e);
        }
    }
}