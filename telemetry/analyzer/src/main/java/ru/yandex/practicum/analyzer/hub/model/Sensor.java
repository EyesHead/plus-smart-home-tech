package ru.yandex.practicum.analyzer.hub.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.analyzer.hub.model.enumconverter.DeviceTypeDatabaseConverter;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;

@Entity
@Table(name = "sensors")
@NoArgsConstructor
@Setter
@Getter
public class Sensor {
    @Id
    private String id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;

    @Column(name = "type", nullable = false, columnDefinition = "smallint")
    @Convert(converter = DeviceTypeDatabaseConverter.class)
    private DeviceTypeAvro type;
}