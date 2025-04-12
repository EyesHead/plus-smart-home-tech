package ru.yandex.practicum.analyzer.event.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.analyzer.event.model.enumconverter.ConditionOperationDatabaseConverter;
import ru.yandex.practicum.analyzer.event.model.enumconverter.ConditionTypeDatabaseConverter;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

@Getter
@Setter
@Entity
@Table(name = "conditions")
@ToString
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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