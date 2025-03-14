package ru.yandex.practicum.aggregator;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Component
public class AggregationStarter {
    private final SnapshotService service;
    private final AggregatorKafkaConsumerConfig consumerConfig;
    private final AggregatorKafkaProducerConfig producerConfig;

    private Consumer<String, SensorEventAvro> consumer;
    private Producer<String, SensorsSnapshotAvro> producer;

    private final static boolean RUNNING = true;

    @PostConstruct
    public void init() {
        // Инициализация консьюмера и продюсера
        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.producer = new KafkaProducer<>(producerConfig.getProperties());

        // Подписка на топик
        consumer.subscribe(List.of(consumerConfig.getTopic()));
        log.info("Подписка на топик {} выполнена", consumerConfig.getTopic());
    }

    @PreDestroy
    public void shutdown() {
        consumer.wakeup(); // Прерывание блокирующего poll()
    }

    public void start() {
        try {
            log.info("Запуск обработчика сообщений");
            while (RUNNING) {
                try {
                    ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(1000));
                    log.debug("Получено {} сообщений", records.count());

                    processRecords(records);
                    commitOffsets();

                } catch (WakeupException e) {
                    if (RUNNING) {
                        log.warn("WakeupException при работающем сервисе");
                    }
                }
            }
        } finally {
            closeResources();
        }
    }

    private void processRecords(ConsumerRecords<String, SensorEventAvro> records) {
        for (ConsumerRecord<String, SensorEventAvro> record : records) {
            SensorEventAvro recordData = record.value();
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
            ProducerRecord<String, SensorsSnapshotAvro> record = new ProducerRecord<>(
                    producerConfig.getTopic(),
                    snapshot
            );

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
        try {
            log.info("Начало закрытия ресурсов");
            producer.flush();
            consumer.commitSync();
        } catch (Exception e) {
            log.error("Ошибка при закрытии ресурсов: {}", e.getMessage(), e);
        } finally {
            consumer.close();
            producer.close();
            log.info("Ресурсы закрыты корректно");
        }
    }
}