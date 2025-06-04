package eyeshead.smarthome.analyzer.hubevent.model.enumconverter;

import eyeshead.smarthome.kafka.telemetry.event.ConditionOperationAvro;

public class ConditionOperationDatabaseConverter extends BaseEnumDatabaseConverter<ConditionOperationAvro>{
    protected ConditionOperationDatabaseConverter() {
        super(ConditionOperationAvro.class);
    }
}
