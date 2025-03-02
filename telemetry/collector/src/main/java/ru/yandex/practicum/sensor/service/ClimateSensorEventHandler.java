package ru.yandex.practicum.sensor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.SensorEventProducerService;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.sensor.mapper.SensorEventMapper;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClimateSensorEventHandler implements SensorEventHandler {
    private final SensorEventProducerService producerService;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto sensorEventProto) {
        log.info("Request - ClimateSensorEvent in proto: {}", sensorEventProto);
        SensorEventAvro sensorEventAvro = SensorEventMapper.map(sensorEventProto);

        producerService.send(sensorEventAvro);
        log.info("Response - climate sensor event in avro: {}", sensorEventAvro);
    }
}