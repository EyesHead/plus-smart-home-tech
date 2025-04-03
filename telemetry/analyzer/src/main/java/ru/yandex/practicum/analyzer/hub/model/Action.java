package ru.yandex.practicum.analyzer.hub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

@Getter
@Setter
@Entity
@Table(name = "actions")
public class Action {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "type", nullable = false)
    ActionTypeAvro type;

    @Column(name = "value")
    Integer value; // не null только при type = SET_VALUE
}