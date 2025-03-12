package ru.yandex.practicum.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.config.CollectorKafkaProducerConfig;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Component
public class HubEventProducerService {
    private CollectorKafkaProducerConfig config;
    private final Producer<Void, HubEventAvro> producer;

    @Autowired
    public HubEventProducerService(CollectorKafkaProducerConfig config) {
        this.producer = new KafkaProducer<>(config.getProperties());
    }

    public void send(HubEventAvro event) {
        ProducerRecord<Void, HubEventAvro> record =
                new ProducerRecord<>(config.getHubTopic(), event);

        producer.send(record);
        producer.flush();
    }
}