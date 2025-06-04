package eyeshead.smarthome.analyzer.hubevent.model.enumconverter;

import eyeshead.smarthome.kafka.telemetry.event.ActionTypeAvro;

public class ActionTypeDatabaseConverter extends BaseEnumDatabaseConverter<ActionTypeAvro> {
    protected ActionTypeDatabaseConverter() {
        super(ActionTypeAvro.class);
    }
}
