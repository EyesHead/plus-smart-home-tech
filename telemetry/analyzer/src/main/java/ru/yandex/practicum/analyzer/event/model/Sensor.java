package ru.yandex.practicum.analyzer.event.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.analyzer.event.model.enumconverter.DeviceTypeDatabaseConverter;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;

@Entity
@Table(name = "sensors")
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Sensor {
    @Id
    private String id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;

    @Column(name = "type", nullable = false, columnDefinition = "smallint")
    @Convert(converter = DeviceTypeDatabaseConverter.class)
    private DeviceTypeAvro type;
}