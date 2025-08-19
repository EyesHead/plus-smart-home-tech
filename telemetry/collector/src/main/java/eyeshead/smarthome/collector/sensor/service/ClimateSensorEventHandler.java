package eyeshead.smarthome.collector.sensor.service;

import eyes.head.smarthome.avro.mapper.TimestampMapper;
import eyeshead.smarthome.collector.kafka.SensorEventProducerService;
import eyeshead.smarthome.grpc.telemetry.event.ClimateSensorProto;
import eyeshead.smarthome.grpc.telemetry.event.SensorEventProto;
import eyeshead.smarthome.kafka.telemetry.event.ClimateSensorAvro;
import eyeshead.smarthome.kafka.telemetry.event.SensorEventAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        log.info("Обработчик сенсора климата получил proto схему данных сенсора");

        ClimateSensorAvro climateSensorAvro = mapPayload(sensorEventProto.getClimateSensorEvent());
        SensorEventAvro sensorEventAvro = SensorEventAvro.newBuilder()
                .setHubId(sensorEventProto.getHubId())
                .setId(sensorEventProto.getId())
                .setTimestamp(TimestampMapper.mapToAvro(sensorEventProto.getTimestamp()))
                .setPayload(climateSensorAvro)
                .build();

        producerService.send(sensorEventAvro);
        log.info("Данные сенсора климата были переведены в формат Avro и успешно отправлены в топик сенсоров: {}",  sensorEventAvro);
    }

    private ClimateSensorAvro mapPayload(ClimateSensorProto climateProto) {
        log.info("Данные сенсора климата: temp={}, humidity={}, co2={}",
                climateProto.getTemperatureC(),
                climateProto.getHumidity(),
                climateProto.getCo2Level());

        return ClimateSensorAvro.newBuilder()
                .setCo2Level(climateProto.getCo2Level())
                .setTemperatureC(climateProto.getTemperatureC())
                .setHumidity(climateProto.getHumidity())
                .build();
    }
}