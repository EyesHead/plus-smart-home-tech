package ru.yandex.practicum.sensor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.sensor.service.SensorEventAvroMapper;
import ru.yandex.practicum.sensor.model.SensorEvent;
import ru.yandex.practicum.sensor.service.SensorEventProducerService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sensors")
public class SensorEventController {
    private final SensorEventProducerService producerService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void collectSensorEvent(@Valid @RequestBody SensorEvent sensorEvent) {
        SensorEventAvro avroMessage = SensorEventAvroMapper.toAvro(sensorEvent);
        producerService.send(avroMessage);
    }
}