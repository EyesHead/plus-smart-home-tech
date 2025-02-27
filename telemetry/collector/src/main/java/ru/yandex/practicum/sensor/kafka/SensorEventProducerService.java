package ru.yandex.practicum.sensor.kafka;

import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaProducerSensorEventConfig;
import ru.yandex.practicum.config.TopicNames;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Component
public class SensorEventProducerService {
    private static final Producer<Void, SensorEventAvro> producer;

    static {
        producer = new KafkaProducer<>(KafkaProducerSensorEventConfig.init());
    }

    public void send(SensorEventAvro event) {
        ProducerRecord<Void, SensorEventAvro> record =
                new ProducerRecord<>(TopicNames.TELEMETRY_SENSORS_TOPIC, event);

        producer.send(record);
        producer.flush();
    }

    @PreDestroy
    public void close() {
        producer.close();
    }
}