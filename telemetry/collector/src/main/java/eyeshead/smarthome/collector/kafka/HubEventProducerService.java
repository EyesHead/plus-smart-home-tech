package eyeshead.smarthome.collector.kafka;

import eyeshead.smarthome.collector.kafka.config.CollectorKafkaProducerConfig;
import eyeshead.smarthome.kafka.telemetry.event.HubEventAvro;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HubEventProducerService {
    private final CollectorKafkaProducerConfig config;
    private Producer<Void, HubEventAvro> producer;

    @PostConstruct
    public void initProducer() {
        this.producer = new KafkaProducer<>(config.getProperties());
    }

    public void send(HubEventAvro event) {
        ProducerRecord<Void, HubEventAvro> record =
                new ProducerRecord<>(config.getHubTopic(), event);

        producer.send(record);
        producer.flush();
    }
}