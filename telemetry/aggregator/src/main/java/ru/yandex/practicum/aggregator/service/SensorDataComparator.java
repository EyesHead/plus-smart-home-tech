package ru.yandex.practicum.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;

import java.util.Objects;

@Slf4j
public class SensorDataComparator {
    public static boolean isEqual(Object data1, Object data2) {
        log.debug("Сравниваем два {}", data1.getClass());

        if (data1 == data2) return true;

        if (data1 instanceof SpecificRecord record1 && data2 instanceof SpecificRecord record2) {

            if (!record1.getSchema().equals(record2.getSchema())) return false;

            // Сравнение всех полей по порядку
            for (int i = 0; i < record1.getSchema().getFields().size(); i++) {
                Object val1 = record1.get(i);
                Object val2 = record2.get(i);
                if (!Objects.deepEquals(val1, val2)) return false;
            }
            return true;
        }
        return false;
    }
}
