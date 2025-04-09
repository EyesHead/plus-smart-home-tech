package ru.yandex.practicum.collector.sensor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.avro.mapper.TimestampMapper;
import ru.yandex.practicum.collector.kafka.SensorEventProducerService;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;

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
        log.info("Обработчик сенсора LightSensorEvent получил proto схему данных с сенсора");
        SwitchSensorAvro switchSensorAvro = mapPayload(sensorEventProto.getSwitchSensorEvent());

        SensorEventAvro sensorEventAvro = SensorEventAvro.newBuilder()
                .setId(sensorEventProto.getId())
                .setTimestamp(TimestampMapper.mapToInstant(sensorEventProto.getTimestamp()))
                .setHubId(sensorEventProto.getHubId())
                .setPayload(switchSensorAvro)
                .build();

        producerService.send(sensorEventAvro);
        log.info("Данные LightSensorEvent были переведены в формат Avro и успешно отправлены в топик сенсоров: {}",  sensorEventAvro);
    }

    private SwitchSensorAvro mapPayload(SwitchSensorProto switchSensorProto) {
        return SwitchSensorAvro.newBuilder()
                .setState(switchSensorProto.getState())
                .build();
    }
}
