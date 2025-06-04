package eyeshead.smarthome.analyzer.hubevent;

import eyeshead.smarthome.analyzer.hubevent.kafka.config.HubEventConsumerConfig;
import eyeshead.smarthome.analyzer.hubevent.service.HubEventService;
import eyeshead.smarthome.kafka.telemetry.event.HubEventAvro;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {
    private final HubEventConsumerConfig consumerConfig;
    private final HubEventService service;

    private Consumer<Void, HubEventAvro> consumer;

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

    @Override
    public void run() {
        log.info("Запуск обработчика событий хаба");
        try {
            while (true) {
                ConsumerRecords<Void, HubEventAvro> records = consumer.poll(consumerConfig.getTimeout());
                if (!records.isEmpty()) {
                    processRecords(records);
                }
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий хаба", e);
        } finally {
            closeResources();
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
