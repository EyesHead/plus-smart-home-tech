package ru.yandex.practicum.analyzer.event.factory;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.event.model.Scenario;
import ru.yandex.practicum.analyzer.event.model.ScenarioCreationRequest;
import ru.yandex.practicum.analyzer.event.model.Sensor;
import ru.yandex.practicum.analyzer.event.service.SensorService;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScenarioFactory {
    private final SensorService sensorService;
    private final ConditionFactory conditionFactory;
    private final ActionFactory actionFactory;

    public Scenario create(ScenarioCreationRequest request) {
        log.info("Создание сценария '{}' для хаба {}", request.name(), request.hubId());

        Scenario scenario = new Scenario();
        scenario.setHubId(request.hubId());
        scenario.setName(request.name());

        Map<String, Sensor> sensors = sensorService.getSensorsByIds(collectSensorIds(request));

        addConditions(scenario, request, sensors);
        addActions(scenario, request, sensors);

        return scenario;
    }

    private Set<String> collectSensorIds(ScenarioCreationRequest request) {
        return Stream.concat(
                request.conditions().stream().map(ScenarioConditionAvro::getSensorId),
                request.actions().stream().map(DeviceActionAvro::getSensorId)
        ).collect(Collectors.toSet());
    }

    private void addConditions(Scenario scenario, ScenarioCreationRequest request, Map<String, Sensor> sensors) {
        request.conditions().forEach(conditionAvro -> {
            Sensor sensor = sensors.get(conditionAvro.getSensorId());
            if (sensor == null) {
                throw new EntityNotFoundException("Sensor не найден в БД. sensorId: " + conditionAvro.getSensorId());
            }
            scenario.getConditions().put(sensor.getId(), conditionFactory.create(conditionAvro));
        });
    }

    private void addActions(Scenario scenario, ScenarioCreationRequest request, Map<String, Sensor> sensors) {
        request.actions().forEach(actionAvro -> {
            Sensor sensor = sensors.get(actionAvro.getSensorId());
            if (sensor == null) {
                throw new EntityNotFoundException("Sensor не найден в БД. sensorId: " + actionAvro.getSensorId());
            }
            scenario.getActions().put(actionAvro.getSensorId(), actionFactory.create(actionAvro));
        });
    }
}