package eyeshead.smarthome.collector.sensor.service;

import eyes.head.smarthome.avro.mapper.TimestampMapper;
import eyeshead.smarthome.collector.kafka.SensorEventProducerService;
import eyeshead.smarthome.grpc.telemetry.event.MotionSensorProto;
import eyeshead.smarthome.grpc.telemetry.event.SensorEventProto;
import eyeshead.smarthome.kafka.telemetry.event.MotionSensorAvro;
import eyeshead.smarthome.kafka.telemetry.event.SensorEventAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MotionSensorEventHandler implements SensorEventHandler {
    private final SensorEventProducerService producerService;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto sensorEventProto) {
        log.info("Обработчик сенсора LightSensorEvent получил proto схему данных с сенсора");
        MotionSensorAvro motionSensorAvro = mapPayload(sensorEventProto.getMotionSensorEvent());

        SensorEventAvro sensorEventAvro = SensorEventAvro.newBuilder()
                .setHubId(sensorEventProto.getHubId())
                .setTimestamp(TimestampMapper.mapToAvro(sensorEventProto.getTimestamp()))
                .setId(sensorEventProto.getId())
                .setPayload(motionSensorAvro)
                .build();

        producerService.send(sensorEventAvro);
        log.info("Данные LightSensorEvent были переведены в формат Avro и успешно отправлены в топик сенсоров: {}",  sensorEventAvro);
    }

    private MotionSensorAvro mapPayload(MotionSensorProto motionSensorProto) {
        return MotionSensorAvro.newBuilder()
                .setMotion(motionSensorProto.getMotion())
                .setVoltage(motionSensorProto.getVoltage())
                .setLinkQuality(motionSensorProto.getLinkQuality())
                .build();
    }
}