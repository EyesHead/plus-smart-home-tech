package eyeshead.smarthome.analyzer.hubevent.service.handler;

import eyeshead.smarthome.analyzer.hubevent.model.Sensor;
import eyeshead.smarthome.analyzer.hubevent.repository.SensorRepository;
import eyeshead.smarthome.kafka.telemetry.event.DeviceAddedEventAvro;
import eyeshead.smarthome.kafka.telemetry.event.HubEventAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
@Slf4j
public class DeviceAddedEventServiceHandler implements HubEventServiceHandler {
    private final SensorRepository repository;

    @Override
    @Transactional
    public void handleEvent(HubEventAvro hubEventAvro) {
        Sensor sensor = createSensor(hubEventAvro);

        repository.save(sensor);
    }

    private Sensor createSensor(HubEventAvro hubEventAvro) {
        log.debug("Добавление сенсора из следующих данных: {}", hubEventAvro);
        DeviceAddedEventAvro sensorData = (DeviceAddedEventAvro) hubEventAvro.getPayload();

        Sensor sensor = new Sensor();

        sensor.setHubId(hubEventAvro.getHubId());
        sensor.setType(sensorData.getType());
        sensor.setId(sensorData.getId());

        log.debug("Данные были переведен в объект Sensor и будут сохранёны в БД: {}", sensor);

        return sensor;
    }

    @Override
    public HubEventServiceType getHandlerType() {
        return HubEventServiceType.DEVICE_ADDED_EVENT;
    }
}
