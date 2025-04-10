package ru.yandex.practicum.collector.sensor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.avro.mapper.TimestampMapper;
import ru.yandex.practicum.collector.kafka.SensorEventProducerService;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class LightSensorEventHandler implements SensorEventHandler {
    private final SensorEventProducerService producerService;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto sensorEventProto) {
        log.info("Обработчик сенсора LightSensorEvent получил proto схему данных с сенсора");

        LightSensorAvro lightSensorAvro = mapPayload(sensorEventProto.getLightSensorEvent());
        SensorEventAvro sensorEventAvro = SensorEventAvro.newBuilder()
                .setHubId(sensorEventProto.getHubId())
                .setId(sensorEventProto.getId())
                .setTimestamp(TimestampMapper.mapToAvro(sensorEventProto.getTimestamp()))
                .setPayload(lightSensorAvro)
                .build();

        producerService.send(sensorEventAvro);
        log.info("Данные LightSensorEvent были переведены в формат Avro и успешно отправлены в топик сенсоров: {}",  sensorEventAvro);
    }

    private LightSensorAvro mapPayload(LightSensorProto sensorEventProto) {
        return LightSensorAvro.newBuilder()
                .setLinkQuality(sensorEventProto.getLinkQuality())
                .setLuminosity(sensorEventProto.getLuminosity())
                .build();
    }
}