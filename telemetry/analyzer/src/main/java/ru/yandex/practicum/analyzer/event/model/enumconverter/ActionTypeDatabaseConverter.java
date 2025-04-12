package ru.yandex.practicum.analyzer.event.model.enumconverter;

import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

public class ActionTypeDatabaseConverter extends BaseEnumDatabaseConverter<ActionTypeAvro> {
    protected ActionTypeDatabaseConverter() {
        super(ActionTypeAvro.class);
    }
}
