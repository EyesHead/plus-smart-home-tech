package ru.yandex.practicum.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.config.CollectorKafkaProducerConfig;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Component
public class SensorEventProducerService {
    private final CollectorKafkaProducerConfig config;
    private final Producer<Void, SensorEventAvro> producer;

    @Autowired
    public SensorEventProducerService(CollectorKafkaProducerConfig config) {
        this.config = config;
        this.producer = new KafkaProducer<>(config.getProperties());
    }

    public void send(SensorEventAvro event) {
        ProducerRecord<Void, SensorEventAvro> record =
                new ProducerRecord<>(config.getSensorTopic(), event);

        producer.send(record);
        producer.flush();
    }
}