package eyeshead.smarthome.analyzer.hubevent.model;

import eyeshead.smarthome.analyzer.hubevent.model.enumconverter.DeviceTypeDatabaseConverter;
import eyeshead.smarthome.kafka.telemetry.event.DeviceTypeAvro;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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