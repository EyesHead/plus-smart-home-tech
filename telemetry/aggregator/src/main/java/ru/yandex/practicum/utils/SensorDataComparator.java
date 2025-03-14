package ru.yandex.practicum.utils;

import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecord;

import java.util.Objects;

public class SensorDataComparator {
    public static boolean isEqual(Object data1, Object data2) {
        if (data1 == data2) return true;
        if (data1 == null || data2 == null) return false;

        if (data1 instanceof SpecificRecord record1 && data2 instanceof SpecificRecord record2) {

            // Проверка типа данных
            if (!record1.getSchema().equals(record2.getSchema())) return false;

            // Сравнение каждого поля
            for (Schema.Field field : record1.getSchema().getFields()) {
                Object value1 = record1.get(field.pos());
                Object value2 = record2.get(field.pos());
                if (!Objects.equals(value1, value2)) return false;
            }
            return true;
        }
        return false;
    }
}
