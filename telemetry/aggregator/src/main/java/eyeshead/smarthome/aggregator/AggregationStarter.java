package eyeshead.smarthome.aggregator;

import eyeshead.smarthome.aggregator.kafka.config.AggregatorConsumerConfig;
import eyeshead.smarthome.aggregator.kafka.config.AggregatorProducerConfig;
import eyeshead.smarthome.aggregator.service.SnapshotService;
import eyeshead.smarthome.kafka.telemetry.event.SensorEventAvro;
import eyeshead.smarthome.kafka.telemetry.event.SensorsSnapshotAvro;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private final SnapshotService service;
    private final AggregatorConsumerConfig consumerConfig;
    private final AggregatorProducerConfig producerConfig;

    private Consumer<Void, SensorEventAvro> consumer;
    private KafkaProducer<Void, SensorsSnapshotAvro> producer;

    @PostConstruct
    private void init() {
        try {
            this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
            this.producer = new KafkaProducer<>(producerConfig.getProperties());

            consumer.subscribe(List.of(consumerConfig.getTopic()));
            log.info("Подписка на топик {} выполнена", consumerConfig.getTopic());
        } catch (Exception e) {
            log.error("Ошибка инициализации Kafka-клиентов: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void start() {
        try {
            log.info("Обработчик сообщений запущен");
            while (true) {
                ConsumerRecords<Void, SensorEventAvro> records = consumer.poll(consumerConfig.getPollTimeout());
                if (!records.isEmpty()) {
                    log.debug("Из топика {} было получено {} сообщений}", consumerConfig.getTopic(), records.count());
                    processRecords(records);
                    log.info("------------------------------------------------");
                }
            }

        } catch (WakeupException ignored) {
            log.info("⚠WakeupException — остановка по инициативе");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            closeResources();
        }
    }

    private void processRecords(ConsumerRecords<Void, SensorEventAvro> records) {
        for (ConsumerRecord<Void, SensorEventAvro> record : records) {
            SensorEventAvro recordData = record.value();
            log.info("Начинаю обработку сообщения: {}", recordData);
            Optional<SensorsSnapshotAvro> snapshotOpt = service.updateState(recordData);
            snapshotOpt.ifPresent(this::sendSnapshot);
        }
    }

    private void sendSnapshot(SensorsSnapshotAvro snapshot) {
        try {
            String topicName = producerConfig.getTopic();
            log.info("Отправка снапшота после обработки в топик {}:\n{}", topicName, snapshot);

            ProducerRecord<Void, SensorsSnapshotAvro> record = new ProducerRecord<>(topicName, snapshot);

            producer.send(record);
            producer.flush();
        } catch (Exception e) {
            log.error("Ошибка отправки сообщения: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    private void closeResources() {
        producer.flush();
        consumer.commitSync();

        consumer.close();
        producer.close();
    }
}