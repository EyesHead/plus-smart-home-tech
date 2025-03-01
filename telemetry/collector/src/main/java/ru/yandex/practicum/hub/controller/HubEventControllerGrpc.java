package ru.yandex.practicum.hub.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.hub.service.HubEventHandler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HubEventControllerGrpc {
    Map<HubEventProto.PayloadCase, HubEventHandler> hubEventHandlers;

    public HubEventControllerGrpc(Set<HubEventHandler> hubEventHandlers) {
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(
                        ru.yandex.practicum.hub.service.HubEventHandler::getMessageType,
                        Function.identity()
                ));
    }

    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        log.info("gRPC collector request for method collectHubEvent: {}", request);
        HubEventProto.PayloadCase hubEventType = request.getPayloadCase();

        try {
            if (!hubEventHandlers.containsKey(hubEventType)) {
                throw new IllegalArgumentException("Can't find handler for event type: " + request.getPayloadCase());
            }

            // Обработка события hub
            hubEventHandlers.get(hubEventType).handle(request);

            // Отправка ответа ПОСЛЕ успешной обработки
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
            log.error("Executing GRPC method collectHubEvent error. {}", e.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        log.info("Registered HubEvent handlers: {}", hubEventHandlers.keySet());
    }
}
