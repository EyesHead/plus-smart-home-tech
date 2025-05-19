package ru.yandex.practicum.collector.hub.controller;

import com.google.protobuf.Empty;
import com.google.protobuf.TextFormat;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.hub.service.HubEventHandler;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HubEventControllerGrpc {
    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubEventHandlers;

    @Autowired
    public HubEventControllerGrpc(Set<HubEventHandler> hubEventHandlers) {
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getMessageType,
                        Function.identity()
                ));
    }

    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        log.info("Пришли данные hubEvent в формате protobuff: {}",
                TextFormat.printer().escapingNonAscii(false).printToString(request));
        HubEventProto.PayloadCase hubEventType = request.getPayloadCase();

        try {
            if (!hubEventHandlers.containsKey(hubEventType)) {
                throw new IllegalArgumentException("Обработчика для указанного типа запроса не существует: " + request.getPayloadCase());
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
            log.error("Выполнение GRPC метода collectHubEvent завершилось с ошибкой. {}", e.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        log.info("Зарегистрированы обработчики событий HubEvent: {}", hubEventHandlers.keySet());
    }
}
