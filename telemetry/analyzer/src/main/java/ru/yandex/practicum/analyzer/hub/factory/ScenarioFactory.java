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
    public Scenario createScenario(HubEventAvro hubEventAvro) {
        Scenario scenario = new Scenario();
        scenario.setHubId(hubEventAvro.getHubId());

        ScenarioAddedEventAvro event = (ScenarioAddedEventAvro) hubEventAvro.getPayload();
        scenario.setName(event.getName());

        // Собираем все ID сенсоров
        Set<String> sensorIds = Stream.concat(
                event.getConditions().stream().map(ScenarioConditionAvro::getSensorId),
                event.getActions().stream().map(DeviceActionAvro::getSensorId)
        ).collect(Collectors.toSet());

        // Загружаем все сенсоры одним запросом
        Map<String, Sensor> sensors = sensorRepository.findAllById(sensorIds).stream()
                .collect(Collectors.toMap(Sensor::getId, Function.identity()));

        // Создаем условия и создаем связи в scenario_conditions
        event.getConditions().forEach(conditionAvro -> {
            Sensor sensor = sensors.get(conditionAvro.getSensorId());
            if (sensor == null) {
                throw new EntityNotFoundException("Sensor not found: " + conditionAvro.getSensorId());
            }

            Condition condition = conditionCreator.create(conditionAvro);
            scenario.getConditions().put(conditionAvro.getSensorId(), condition);
        });

        // Создаем действия и создаем связи в scenario_actions
        event.getActions().forEach(actionAvro -> {
            Sensor sensor = sensors.get(actionAvro.getSensorId());
            if (sensor == null) {
                throw new EntityNotFoundException("Sensor not found: " + actionAvro.getSensorId());
            }

            Action action = actionCreator.create(actionAvro);
            scenario.getActions().put(actionAvro.getSensorId(), action);
        });

        return scenario;
    }
}