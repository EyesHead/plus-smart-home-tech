package ru.yandex.practicum.analyzer.hub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;

@Entity
@Table(schema = "sensors")
@NoArgsConstructor
@Setter
@Getter
public class Sensor {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;

    @Column(name = "type", nullable = false)
    private DeviceTypeAvro type;
}
