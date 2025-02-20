package ru.yandex.practicum.hub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.hub.model.HubEvent;
import ru.yandex.practicum.hub.service.HubEventAvroMapper;
import ru.yandex.practicum.hub.service.HubEventProducerService;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/hubs")
public class HubEventController {
    private final HubEventProducerService producerService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void collectHubEvent(@Valid @RequestBody HubEvent hubEvent) {
        HubEventAvro avroMessage = HubEventAvroMapper.toAvro(hubEvent);
        producerService.send(avroMessage);
    }
}