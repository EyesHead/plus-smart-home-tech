package ru.yandex.practicum.analyzer.hub.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.analyzer.hub.model.enumconverter.ActionTypeDatabaseConverter;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

@Getter
@Setter
@Entity
@Table(name = "actions")
@ToString
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, columnDefinition = "smallint")
    @Convert(converter = ActionTypeDatabaseConverter.class)
    private ActionTypeAvro type;

    @Column(name = "value")
    private Integer value; // не null только при type = SET_VALUE
}