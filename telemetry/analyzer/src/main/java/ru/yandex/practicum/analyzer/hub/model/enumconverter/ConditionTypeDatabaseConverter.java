package ru.yandex.practicum.analyzer.hub.model.enumconverter;

import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

public class ConditionTypeDatabaseConverter extends BaseEnumDatabaseConverter<ConditionTypeAvro> {
    protected ConditionTypeDatabaseConverter() {
        super(ConditionTypeAvro.class);
    }
}
