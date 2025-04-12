package ru.yandex.practicum.analyzer.hub.factory;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.hub.model.Action;
import ru.yandex.practicum.analyzer.hub.model.Condition;
import ru.yandex.practicum.analyzer.hub.model.Scenario;
import ru.yandex.practicum.analyzer.hub.model.Sensor;
import ru.yandex.practicum.analyzer.hub.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ScenarioFactory {
    private final SensorRepository sensorRepository;
    private final ActionCreator actionCreator;
    private final ConditionCreator conditionCreator;

    @Transactional
    public Scenario createScenario(HubEventAvro hubEventAvro, ScenarioAddedEventAvro scenarioAvro) {
        Scenario scenario = new Scenario();
        scenario.setHubId(hubEventAvro.getHubId());
        scenario.setName(scenarioAvro.getName());

        // Собираем все ID сенсоров
        Set<String> sensorIds = Stream.concat(
                scenarioAvro.getConditions().stream().map(ScenarioConditionAvro::getSensorId),
                scenarioAvro.getActions().stream().map(DeviceActionAvro::getSensorId)
        ).collect(Collectors.toSet());

        // Загружаем все сенсоры одним запросом
        Map<String, Sensor> sensors = sensorRepository.findAllById(sensorIds).stream()
                .collect(Collectors.toMap(Sensor::getId, Function.identity()));

        // Создаем условия и создаем связи в scenario_conditions
        scenarioAvro.getConditions().forEach(scenarioConditionAvro -> {
            Sensor sensor = sensors.get(scenarioConditionAvro.getSensorId());
            if (sensor == null) {
                throw new EntityNotFoundException("Sensor не найден в БД. sensorId = " +
                        scenarioConditionAvro.getSensorId());
            }

            Condition condition = conditionCreator.create(scenarioConditionAvro);
            scenario.getConditions().put(sensor.getId(), condition);
        });

        // Создаем действия и создаем связи в scenario_actions
        scenarioAvro.getActions().forEach(actionAvro -> {
            Sensor sensor = sensors.get(actionAvro.getSensorId());
            if (sensor == null) {
                throw new EntityNotFoundException("Sensor не найден в БД. sensorId: " + actionAvro.getSensorId());
            }
            Action action = actionCreator.create(actionAvro);
            scenario.getActions().put(actionAvro.getSensorId(), action);
        });

        return scenario;
    }
}