package ru.yandex.practicum.analyzer.event.model.enumconverter;

import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;

public class DeviceTypeDatabaseConverter extends BaseEnumDatabaseConverter<DeviceTypeAvro> {
    public DeviceTypeDatabaseConverter() {
        super(DeviceTypeAvro.class);
    }
}
