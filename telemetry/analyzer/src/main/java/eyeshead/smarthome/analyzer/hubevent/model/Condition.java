package eyeshead.smarthome.analyzer.hubevent.model;

import eyeshead.smarthome.analyzer.hubevent.model.enumconverter.ConditionOperationDatabaseConverter;
import eyeshead.smarthome.analyzer.hubevent.model.enumconverter.ConditionTypeDatabaseConverter;
import eyeshead.smarthome.kafka.telemetry.event.ConditionOperationAvro;
import eyeshead.smarthome.kafka.telemetry.event.ConditionTypeAvro;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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