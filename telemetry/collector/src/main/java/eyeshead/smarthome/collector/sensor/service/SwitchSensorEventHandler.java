package eyeshead.smarthome.collector.sensor.service;

import eyes.head.smarthome.avro.mapper.TimestampMapper;
import eyeshead.smarthome.collector.kafka.SensorEventProducerService;
import eyeshead.smarthome.grpc.telemetry.event.SensorEventProto;
import eyeshead.smarthome.grpc.telemetry.event.SwitchSensorProto;
import eyeshead.smarthome.kafka.telemetry.event.SensorEventAvro;
import eyeshead.smarthome.kafka.telemetry.event.SwitchSensorAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
                .setTimestamp(TimestampMapper.mapToAvro(sensorEventProto.getTimestamp()))
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
