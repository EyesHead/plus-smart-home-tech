package ru.yandex.practicum.sensor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.sensor.mapper.SensorEventAvroMapper;
import ru.yandex.practicum.sensor.model.SensorEvent;
import ru.yandex.practicum.sensor.service.SensorEventProducerService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sensors")
public class SensorEventController {
    private final SensorEventProducerService producerService;

    @PostMapping
    public void collectSensorEvent(@Valid @RequestBody SensorEvent sensorEvent) {
        ru.yandex.practicum.kafka.telemetry.event.SensorEvent avroMessage = SensorEventAvroMapper.toAvro(sensorEvent);
        producerService.send(avroMessage);
    }
}