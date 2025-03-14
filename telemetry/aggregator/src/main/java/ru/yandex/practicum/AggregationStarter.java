package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class AggregationStarter {
    private final SnapshotService service;
    private final AggregatorKafkaConsumerConfig consumerConfig;
    private final AggregatorKafkaProducerConfig producerConfig;

    public void start() {
        Consumer<String, SensorEventAvro> consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        Producer<String, SensorsSnapshotAvro> producer = new KafkaProducer<>(producerConfig.getProperties());
        try {
            consumer.subscribe(List.of(consumerConfig.getTopic()));

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records =
                        consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    log.info("Получено сообщение из партиции {}, со смещением {}:\n{}\n",
                            record.partition(), record.offset(), record.value());

                    Optional<SensorsSnapshotAvro> aggregatedSnapshot = service.updateState(record.value());

                    // реализация отправки агрегированных данных из сервиса в kafka topic
                    aggregatedSnapshot.ifPresent(snapshot -> {
                        ProducerRecord<String, SensorsSnapshotAvro> snapshotRecord = new ProducerRecord<>(
                                producerConfig.getTopic(),
                                snapshot.getHubId(),
                                snapshot
                        );
                        producer.send(snapshotRecord, (metadata,  exception) -> {
                            if (exception != null) {
                                log.error("Ошибка отправки снапшота: {}", exception.getMessage());
                            }
                        });
                    });
                }
            }
        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                // Сбрасываем все сообщения из буфера продюсера
                producer.flush();

                // Фиксируем смещения обработанных сообщений в консьюмере
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }
}