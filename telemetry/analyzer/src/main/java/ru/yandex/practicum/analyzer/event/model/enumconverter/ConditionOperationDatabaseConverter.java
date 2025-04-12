package ru.yandex.practicum.analyzer.event.model.enumconverter;

import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;

public class ConditionOperationDatabaseConverter extends BaseEnumDatabaseConverter<ConditionOperationAvro>{
    protected ConditionOperationDatabaseConverter() {
        super(ConditionOperationAvro.class);
    }
}
