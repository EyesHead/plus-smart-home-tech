package eyeshead.smarthome.analyzer.hubevent.model;

import eyeshead.smarthome.analyzer.hubevent.model.enumconverter.ActionTypeDatabaseConverter;
import eyeshead.smarthome.kafka.telemetry.event.ActionTypeAvro;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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