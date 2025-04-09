package ru.yandex.practicum.analyzer.hub.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.analyzer.hub.model.enumconverter.ConditionOperationDatabaseConverter;
import ru.yandex.practicum.analyzer.hub.model.enumconverter.ConditionTypeDatabaseConverter;
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

    @Column(name = "type", nullable = false, columnDefinition = "smallint")
    @Convert(converter = ConditionTypeDatabaseConverter.class)
    private ConditionTypeAvro type;

    @Column(name = "operation", nullable = false, columnDefinition = "smallint")
    @Convert(converter = ConditionOperationDatabaseConverter.class)
    private ConditionOperationAvro operation;

    @Column(name = "value")
    private Integer value;
}