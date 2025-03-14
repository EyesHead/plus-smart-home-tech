package ru.yandex.practicum;

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
import ru.yandex.practicum.kafka.config.AggregatorKafkaConsumerConfig;
import ru.yandex.practicum.kafka.config.AggregatorKafkaProducerConfig;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.SnapshotService;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Класс AggregationStarter, ответственный за запуск агрегации данных.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AggregationStarter implements Runnable {
    private final SnapshotService service;
    private final AggregatorKafkaConsumerConfig consumerConfig;
    private final AggregatorKafkaProducerConfig producerConfig;

    private Consumer<String, SensorEventAvro> consumer;
    private Producer<String, SensorsSnapshotAvro> producer;

    private volatile boolean running = true;

    @PostConstruct
    public void init() {
        // Инициализация консьюмера и продюсера
        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.producer = new KafkaProducer<>(producerConfig.getProperties());

        // Подписка на топик
        consumer.subscribe(List.of(consumerConfig.getTopic()));
        log.info("Подписка на топик {} выполнена", consumerConfig.getTopic());

        // Запуск обработки в отдельном потоке
        new Thread(this).start();
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        consumer.wakeup(); // Прерывание блокирующего poll()
    }

    @Override
    public void run() {
        try {
            log.info("Запуск обработчика сообщений");
            while (running) {
                try {
                    ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(1000));
                    log.debug("Получено {} сообщений", records.count());

                    processRecords(records);
                    commitOffsets();

                } catch (WakeupException e) {
                    if (running) {
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
            try {
                log.info("Payload type: {}", record.value().getPayload().getClass());
                Optional<SensorsSnapshotAvro> snapshot = service.updateState(record.value());
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
                    snapshot.getHubId(),
                    snapshot
            );

            producer.send(record, (metadata, ex) -> {
                if (ex != null) {
                    log.error("Ошибка отправки снапшота: {}", ex.getMessage(), ex);
                } else {
                    log.debug("Снапшот отправлен в партицию {}", metadata.partition());
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