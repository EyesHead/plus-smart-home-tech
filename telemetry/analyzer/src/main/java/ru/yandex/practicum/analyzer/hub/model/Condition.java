package ru.yandex.practicum.analyzer.hub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

@Getter
@Setter
@Entity
@Table(name = "conditions")
public class Condition {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "type", nullable = false)
    private ConditionTypeAvro type;

    @Column(name = "operation", nullable = false)
    private ConditionOperationAvro operation;

    @Column(name = "value")
    private Integer value;
}