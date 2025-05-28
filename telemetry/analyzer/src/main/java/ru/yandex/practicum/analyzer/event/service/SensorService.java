package ru.yandex.practicum.analyzer.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.event.model.Sensor;
import ru.yandex.practicum.analyzer.event.repository.SensorRepository;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorService {
    private final SensorRepository sensorRepository;

    public Map<String, Sensor> getSensorsByIds(Set<String> sensorIds) {
        return sensorRepository.findAllById(sensorIds).stream()
                .collect(Collectors.toMap(Sensor::getId, Function.identity()));
    }
}