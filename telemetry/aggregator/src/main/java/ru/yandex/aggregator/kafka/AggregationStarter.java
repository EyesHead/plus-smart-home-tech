package ru.yandex.aggregator.kafka;

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
import ru.yandex.aggregator.service.SnapshotService;
import ru.yandex.collector.kafka.KafkaProducerConfig;
import ru.yandex.collector.kafka.TopicNames;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;

/**
 * Класс AggregationStarter, ответственный за запуск агрегации данных.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AggregationStarter {
    private final SnapshotService service;
    private final Consumer<String, SensorEventAvro> consumer = new KafkaConsumer<>(SnapshotsConsumerConfig.init());
    private final Producer<String, SensorsSnapshotAvro> producer = new KafkaProducer<>(KafkaProducerConfig.init());


    public void start() {
        try {
            consumer.subscribe(Collections.singleton(TopicNames.TELEMETRY_SENSORS_TOPIC));

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records =
                        consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    log.info("Получено сообщение из партиции {}, со смещением {}:\n{}\n",
                            record.partition(), record.offset(), record.value());

                    Optional<SensorsSnapshotAvro> aggregatedSnapshot = service.updateState(record.value());

                    // реализация отправки агрегированных данных из сервиса в kafka topic
                    aggregatedSnapshot.ifPresent((snapshot) -> producer.send(new ProducerRecord<>(
                            TopicNames.TELEMETRY_SNAPSHOTS_TOPIC,
                            snapshot)));
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