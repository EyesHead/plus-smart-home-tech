package eyeshead.smarthome.collector.sensor.service;

import eyes.head.smarthome.avro.mapper.TimestampMapper;
import eyeshead.smarthome.collector.kafka.SensorEventProducerService;
import eyeshead.smarthome.grpc.telemetry.event.LightSensorProto;
import eyeshead.smarthome.grpc.telemetry.event.SensorEventProto;
import eyeshead.smarthome.kafka.telemetry.event.LightSensorAvro;
import eyeshead.smarthome.kafka.telemetry.event.SensorEventAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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