package ru.yandex.practicum.analyzer.event;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.event.kafka.config.HubEventConsumerConfig;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {
    // Инициализация консьюмера и продюсера
    private Consumer<Void, HubEventAvro> consumer;
    private final HubEventConsumerConfig consumerConfig;

    private final HubEventService service;

    @Override
    public void run() {
        log.info("Запуск обработчика сообщений");
        try {
            while (true) {
                ConsumerRecords<Void, HubEventAvro> records = consumer.poll(Duration.ofMillis(500));
                if (!records.isEmpty()) {
                    processRecords(records);
                    log.info("------------------------------------------------------");
                }
            }
        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий хаба", e);
        } finally {
            closeResources();
        }
    }

    @PostConstruct
    private void init() {
        try {
            this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
            consumer.subscribe(List.of(consumerConfig.getTopic()));
            log.info("Подписка на топик {} выполнена", consumerConfig.getTopic());
        } catch (Exception e) {
            log.error("Ошибка инициализации Kafka-клиентов: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void processRecords(ConsumerRecords<Void, HubEventAvro> records) {
        for (ConsumerRecord<Void, HubEventAvro> record : records) {
            HubEventAvro hubEventAvro = record.value();

            service.handleRequestEvent(hubEventAvro);
        }
    }

    private void closeResources() {
        consumer.commitSync();

        consumer.close();
    }
}
