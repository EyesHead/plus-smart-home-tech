package ru.yandex.practicum.analyzer.snapshot;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequestProto;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Service
@Slf4j
public class ActionProducerService extends HubRouterControllerGrpc.HubRouterControllerImplBase {
    @GrpcClient("hub-router") // внедрение с параметром "hub-router" — это отсылка к блоку конфигурации hub-router
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterService;

    public void sendAction(DeviceActionRequestProto deviceAction) {
        try {
            // отправка сообщения с помощью готового к использованию метода
            hubRouterService.handleDeviceAction(deviceAction);
            log.info("Action was send");
        } catch (StatusRuntimeException e) {
            log.error("Failed to send action: {}", e.getStatus(), e);
        }
    }
}
