package ru.yandex.collector.sensor.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.collector.sensor.service.SensorEventHandler;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SensorEventControllerGrpc {
    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorEventHandlers;

    public SensorEventControllerGrpc(Set<SensorEventHandler> sensorEventHandlers) {
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(
                        SensorEventHandler::getMessageType,
                        Function.identity()
                ));
    }

    /**
     * Метод для обработки событий от датчиков.
     * Вызывается при получении нового события от gRPC-клиента.
     *
     * @param request           Событие от датчика
     * @param responseObserver  Ответ для клиента - Empty
     */
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        log.info("gRPC collector request for method collectHubEvent: {}", request);
        SensorEventProto.PayloadCase sensorType = request.getPayloadCase();

        try {
            if (!sensorEventHandlers.containsKey(sensorType)) {
                throw new IllegalArgumentException("Can't find handler for event type: " + sensorType);
            }

            // Обработка события sensor
            sensorEventHandlers.get(sensorType).handle(request);

            // Отправка ответа ПОСЛЕ успешной обработки
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
            log.error("Executing gRPC method collectSensorEvent error. {}", e.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        log.info("Registered HubEvent handlers: {}", sensorEventHandlers.keySet());
    }
}
