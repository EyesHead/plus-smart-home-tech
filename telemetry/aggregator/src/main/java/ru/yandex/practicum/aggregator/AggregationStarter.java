package ru.yandex.practicum.aggregator;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.UnresolvedUnionException;
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

    private Consumer<String, SensorEventAvro> consumer;
    private Producer<String, SensorsSnapshotAvro> producer;

    @Autowired
    public AggregationStarter(SnapshotService service,
                              AggregatorKafkaConsumerConfig consumerConfig,
                              AggregatorKafkaProducerConfig producerConfig) {
        this.service = service;
        this.consumerConfig = consumerConfig;
        this.producerConfig = producerConfig;
    }

    public void start() {
        try {
            log.info("Запуск обработчика сообщений");
            init();
            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(500));
                if (!records.isEmpty()) {
                    log.debug("Было получено {} сообщений}", records.count());
                }
                processRecords(records);
                commitOffsets();
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            closeResources();
        }
    }

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

    private void processRecords(ConsumerRecords<String, SensorEventAvro> records) {
        for (ConsumerRecord<String, SensorEventAvro> record : records) {
            SensorEventAvro recordData = record.value();
            log.info("Начинаю обработку сообщения: {}", recordData);
            try {
                log.info("Payload type: {}", recordData.getPayload().getClass());
                Optional<SensorsSnapshotAvro> snapshot = service.updateState(recordData);
                snapshot.ifPresent(this::sendSnapshot);
            } catch (UnresolvedUnionException e) {
                log.error("Некорректный тип payload: {}", record.value().getPayload());
            }
        }
    }

    private void sendSnapshot(SensorsSnapshotAvro snapshot) {
        try {
            log.info("Отправка снапшота: hubId={}, timestamp={}", snapshot.getHubId(), snapshot.getTimestamp());

            ProducerRecord<String, SensorsSnapshotAvro> record = new ProducerRecord<>(producerConfig.getTopic(), snapshot);

            producer.send(record, (metadata, ex) -> {
                if (ex != null) {
                    log.error("Ошибка отправки снапшота: {}", ex.getMessage(), ex);
                } else {
                    log.info("Снапшот отправлен в партицию {}", metadata.partition());
                }
            });
        } catch (Exception e) {
            log.error("Ошибка отправки сообщения: {}", e.getMessage(), e);
        }
    }

    private void commitOffsets() {
        try {
            if (!consumer.subscription().isEmpty()) {
                consumer.commitSync();
            }
        } catch (Exception e) {
            log.error("Ошибка коммита оффсетов: {}", e.getMessage(), e);
        }
    }

    private void closeResources() {
        log.info("Начало закрытия ресурсов");
        producer.flush();
        consumer.commitSync();

        consumer.close();
        producer.close();
        log.info("Ресурсы закрыты корректно");
    }
}