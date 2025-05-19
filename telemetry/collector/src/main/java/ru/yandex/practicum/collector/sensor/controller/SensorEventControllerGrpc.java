package ru.yandex.practicum.collector.sensor.controller;

import com.google.protobuf.Empty;
import com.google.protobuf.TextFormat;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.sensor.service.SensorEventHandler;
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
        log.info("Пришли данные сенсора в формате protobuff: {}",
                TextFormat.printer().escapingNonAscii(false).printToString(request));
        SensorEventProto.PayloadCase sensorType = request.getPayloadCase();

        try {
            if (!sensorEventHandlers.containsKey(sensorType)) {
                throw new IllegalArgumentException("Обработчик для сенсора с указанным типом payloadCase не существует: " + sensorType);
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
        log.info("Зарегистрированы обработчики для следующих сенсоров: {}", sensorEventHandlers.keySet());
    }
}
