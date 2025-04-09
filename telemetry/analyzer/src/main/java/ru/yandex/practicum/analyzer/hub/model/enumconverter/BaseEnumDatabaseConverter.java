package ru.yandex.practicum.analyzer.hub.model.enumconverter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public abstract class BaseEnumDatabaseConverter<T extends Enum<T>> implements AttributeConverter<T, Short> {

    private final Class<T> enumClass;

    protected BaseEnumDatabaseConverter(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public Short convertToDatabaseColumn(T attribute) {
        if (attribute == null) return null;
        return (short) attribute.ordinal();
    }

    @Override
    public T convertToEntityAttribute(Short dbData) {
        if (dbData == null) return null;
        T[] values = enumClass.getEnumConstants();
        if (dbData < 0 || dbData >= values.length) {
            throw new IllegalArgumentException("Unknown database value: " + dbData);
        }
        return values[dbData];
    }
}