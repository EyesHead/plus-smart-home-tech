package ru.yandex.collector.sensor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.collector.kafka.SensorEventProducerService;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.collector.sensor.mapper.SensorEventMapper;

@Slf4j
@RequiredArgsConstructor
@Component
public class TemperatureEventHandler implements SensorEventHandler {
    private final SensorEventProducerService producerService;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto sensorEventProto) {
        log.info("Request - TemperatureSensorEvent in proto: {}", sensorEventProto);
        SensorEventAvro sensorEventAvro = SensorEventMapper.map(sensorEventProto);

        producerService.send(sensorEventAvro);
        log.info("Response - temperature sensor event in avro: {}", sensorEventAvro);
    }
}