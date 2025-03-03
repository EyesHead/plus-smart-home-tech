package ru.yandex.practicum.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Component
public class HubEventProducerService {
    private static final Producer<Void, HubEventAvro> producer;

    static {
        producer = new KafkaProducer<>(KafkaProducerConfig.init());
    }

    public void send(HubEventAvro event) {
        ProducerRecord<Void, HubEventAvro> record =
                new ProducerRecord<>(TopicNames.TELEMETRY_HUB_TOPIC, event);

        producer.send(record);
        producer.flush();
    }
}
