package eyeshead.smarthome.analyzer.hubevent.model.enumconverter;

import eyeshead.smarthome.kafka.telemetry.event.DeviceTypeAvro;

public class DeviceTypeDatabaseConverter extends BaseEnumDatabaseConverter<DeviceTypeAvro> {
    public DeviceTypeDatabaseConverter() {
        super(DeviceTypeAvro.class);
    }
}
