package ru.yandex.practicum.sensor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.sensor.kafka.SensorEventProducerService;
import ru.yandex.practicum.sensor.mapper.SensorEventAvroMapper;

@Slf4j
@RequiredArgsConstructor
@Component
public class SwitchSensorEventHandler implements SensorEventHandler {
    private final SensorEventProducerService producerService;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto sensorEventProto) {
        log.info("Request - SwitchSensorEvent in proto: {}", sensorEventProto);
        SensorEventAvro sensorEventAvro = SensorEventAvroMapper
                .mapToAvro(
                        sensorEventProto,
                        SensorEventProto.PayloadCase.SWITCH_SENSOR_EVENT);

        producerService.send(sensorEventAvro);
        log.info("Response - switch sensor event in avro: {}", sensorEventAvro);
    }
}
