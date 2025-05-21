package ru.yandex.practicum.aggregator;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.kafka.config.AggregatorKafkaConsumerConfig;
import ru.yandex.practicum.aggregator.kafka.config.AggregatorKafkaProducerConfig;
import ru.yandex.practicum.aggregator.service.SnapshotService;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Класс AggregationStarter, ответственный за запуск агрегации данных.
 */
@Slf4j
@Component
public class AggregationStarter {
    private final SnapshotService service;
    private final AggregatorKafkaConsumerConfig consumerConfig;
    private final AggregatorKafkaProducerConfig producerConfig;

    private Consumer<Void, SensorEventAvro> consumer;
    private Producer<Void, SensorsSnapshotAvro> producer;

    @Autowired
    public AggregationStarter(SnapshotService service,
                              AggregatorKafkaConsumerConfig consumerConfig,
                              AggregatorKafkaProducerConfig producerConfig) {
        this.service = service;
        this.consumerConfig = consumerConfig;
        this.producerConfig = producerConfig;
    }

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
            log.info("Запуск обработчика сообщений");
            while (true) {
                ConsumerRecords<Void, SensorEventAvro> records = consumer.poll(Duration.ofMillis(500));
                if (!records.isEmpty()) {
                    log.debug("Из топика {} было получено {} сообщений}", consumerConfig.getTopic(), records.count());
                    processRecords(records);
                    log.info("------------------------------------------------");
                }
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
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
            log.info("Отправка снапшота после обработки в топик {}", topicName);

            ProducerRecord<Void, SensorsSnapshotAvro> record = new ProducerRecord<>(topicName, snapshot);

            producer.send(record);
            producer.flush();
        } catch (Exception e) {
            log.error("Ошибка отправки сообщения: {}", e.getMessage(), e);
        }
    }

    private void closeResources() {
        log.debug("Начало закрытия ресурсов");
        producer.flush();
        consumer.commitSync();

        consumer.close();
        producer.close();
        log.info("Ресурсы закрыты");
    }
}