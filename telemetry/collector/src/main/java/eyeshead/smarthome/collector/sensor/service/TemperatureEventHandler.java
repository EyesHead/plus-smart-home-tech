package eyeshead.smarthome.collector.sensor.service;

import eyes.head.smarthome.avro.mapper.TimestampMapper;
import eyeshead.smarthome.collector.kafka.SensorEventProducerService;
import eyeshead.smarthome.grpc.telemetry.event.SensorEventProto;
import eyeshead.smarthome.grpc.telemetry.event.TemperatureSensorProto;
import eyeshead.smarthome.kafka.telemetry.event.SensorEventAvro;
import eyeshead.smarthome.kafka.telemetry.event.TemperatureSensorAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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