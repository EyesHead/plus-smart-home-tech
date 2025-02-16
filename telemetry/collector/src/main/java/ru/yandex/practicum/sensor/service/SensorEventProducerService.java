package ru.yandex.practicum.sensor.service;

import ru.yandex.practicum.config.KafkaSensorEventProperties;
import ru.yandex.practicum.config.TopicNames;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEvent;

@Component
public class SensorEventProducerService {
    private Producer<Void, SensorEvent> producer;

    public void send(SensorEvent event) {
        if (producer == null) {
            producer = new KafkaProducer<>(KafkaSensorEventProperties.init());
        }

        ProducerRecord<Void, SensorEvent> record =
                new ProducerRecord<>(TopicNames.TELEMETRY_SENSORS_TOPIC, event);

        producer.send(record);
        producer.flush();
    }

//    public void close() {
//        producer.close();
//    }
}
