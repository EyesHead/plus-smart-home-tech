package ru.yandex.practicum.analyzer.event.factory;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.event.model.Action;
import ru.yandex.practicum.analyzer.event.model.Condition;
import ru.yandex.practicum.analyzer.event.model.Scenario;
import ru.yandex.practicum.analyzer.event.model.Sensor;
import ru.yandex.practicum.analyzer.event.repository.SensorRepository;
import ru.yandex.practicum.analyzer.event.service.ActionService;
import ru.yandex.practicum.analyzer.event.service.ConditionService;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioFactory {
    private final SensorRepository sensorRepository;
    private final ConditionService conditionService;
    private final ActionService actionService;

    @Transactional
    public Scenario createScenario(HubEventAvro hubEventAvro, ScenarioAddedEventAvro scenarioAvro) {

        log.info("Создание нового сценария с именем '{}' для хаба {}", scenarioAvro.getName(), hubEventAvro.getHubId());
        Scenario scenario = new Scenario();

        scenario.setHubId(hubEventAvro.getHubId());
        scenario.setName(scenarioAvro.getName());

        // Сбор всех идентификаторов сенсоров
        Set<String> sensorIds = collectAllSensorIds(scenarioAvro);
        log.debug("Собраны идентификаторы сенсоров: {}", sensorIds);

        // Получение всех сенсоров сразу одним запросом
        Map<String, Sensor> sensors = loadSensors(sensorIds);
        log.debug("Получены сенсоры: {}", sensors.keySet());

        // Создание и добавление условий
        Scenario scenarioWithConditions = addConditionsToScenario(scenario, scenarioAvro, sensors);
        log.debug("Добавлены условия к сценарию: {}", scenarioWithConditions.getConditions());

        // Создание и добавление действий
        Scenario scenarioFullyUpdated = addActionsToScenario(scenarioWithConditions, scenarioAvro, sensors);
        log.debug("Добавлены действия к сценарию: {}", scenarioFullyUpdated.getActions());

        return scenarioFullyUpdated;
    }

    private Set<String> collectAllSensorIds(ScenarioAddedEventAvro scenarioAvro) {
        return Stream.concat(
                scenarioAvro.getConditions().stream().map(ScenarioConditionAvro::getSensorId),
                scenarioAvro.getActions().stream().map(DeviceActionAvro::getSensorId)
        ).collect(Collectors.toSet());
    }

    private Map<String, Sensor> loadSensors(Set<String> sensorIds) {
        return sensorRepository.findAllById(sensorIds).stream()
                .collect(Collectors.toMap(Sensor::getId, Function.identity()));
    }

    private Scenario addConditionsToScenario(Scenario scenario,
                                             ScenarioAddedEventAvro scenarioAvro,
                                             Map<String, Sensor> sensors) {
        scenarioAvro.getConditions().forEach(conditionAvro -> {
            Sensor sensor = sensors.get(conditionAvro.getSensorId());
            if (sensor == null) {
                throw new EntityNotFoundException("Sensor не найден в БД. sensorId = " +
                        conditionAvro.getSensorId());
            }
            Condition condition = conditionService.save(conditionAvro);
            log.debug("Condition сохранён в БД: {}", condition);
            scenario.getConditions().put(sensor.getId(), condition);
        });
        return scenario;
    }

    private Scenario addActionsToScenario(Scenario scenario,
                                          ScenarioAddedEventAvro scenarioAvro,
                                          Map<String, Sensor> sensors) {
        scenarioAvro.getActions().forEach(actionAvro -> {
            Sensor sensor = sensors.get(actionAvro.getSensorId());
            if (sensor == null) {
                throw new EntityNotFoundException("Sensor не найден в БД. sensorId: " +
                        actionAvro.getSensorId());
            }
            Action action = actionService.save(actionAvro);
            log.debug("Action был успешно создан и сохранён в БД: {}", action);
            scenario.getActions().put(actionAvro.getSensorId(), action);
        });
        return scenario;
    }
}