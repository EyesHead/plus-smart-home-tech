package ru.yandex.practicum.collector.sensor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.avro.mapper.TimestampMapper;
import ru.yandex.practicum.collector.kafka.SensorEventProducerService;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

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
        log.info("Обработчик сенсора LightSensorEvent получил proto схему данных с сенсора");
        TemperatureSensorAvro temperatureSensorAvro = mapPayload(sensorEventProto.getTemperatureSensorEvent());

        SensorEventAvro sensorEventAvro = SensorEventAvro.newBuilder()
                .setId(sensorEventProto.getId())
                .setHubId(sensorEventProto.getHubId())
                .setTimestamp(TimestampMapper.mapToAvro(sensorEventProto.getTimestamp()))
                .setPayload(temperatureSensorAvro)
                .build();

        producerService.send(sensorEventAvro);
        log.info("Данные LightSensorEvent были переведены в формат Avro и успешно отправлены в топик сенсоров: {}",  sensorEventAvro);
    }

    private TemperatureSensorAvro mapPayload(TemperatureSensorProto temperatureSensorProto) {
        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(temperatureSensorProto.getTemperatureC())
                .setTemperatureF(temperatureSensorProto.getTemperatureF())
                .build();
    }
}