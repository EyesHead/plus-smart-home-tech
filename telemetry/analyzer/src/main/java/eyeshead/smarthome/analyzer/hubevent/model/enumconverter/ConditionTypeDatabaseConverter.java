package eyeshead.smarthome.analyzer.hubevent.model.enumconverter;

import eyeshead.smarthome.kafka.telemetry.event.ConditionTypeAvro;

public class ConditionTypeDatabaseConverter extends BaseEnumDatabaseConverter<ConditionTypeAvro> {
    protected ConditionTypeDatabaseConverter() {
        super(ConditionTypeAvro.class);
    }
}
