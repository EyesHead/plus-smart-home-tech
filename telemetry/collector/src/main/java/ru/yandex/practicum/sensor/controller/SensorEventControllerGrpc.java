package ru.yandex.practicum.sensor.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.sensor.service.SensorEventHandler;

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
        SensorEventProto.PayloadCase sensorType = request.getPayloadCase();

        try {
            if (!sensorEventHandlers.containsKey(sensorType)) {
                String errorMessage = String.format("Event handler for type doesn't exist. %s", sensorType);
                log.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            sensorEventHandlers.get(sensorType).handle(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }
}
