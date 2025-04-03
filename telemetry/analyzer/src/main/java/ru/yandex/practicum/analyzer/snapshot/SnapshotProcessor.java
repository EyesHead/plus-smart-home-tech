package ru.yandex.practicum.analyzer.snapshot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.snapshot.kafka.config.SnapshotsConsumerConfig;
import ru.yandex.practicum.analyzer.snapshot.service.SnapshotRequestService;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequestProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SnapshotProcessor {
    private Consumer<Void, SensorsSnapshotAvro> snapshotConsumer;

    private final SnapshotRequestService snapshotService;
    private final SnapshotsConsumerConfig consumerConfig;
    private final ActionProducerService actionProducer;

    public void start() {
        log.info("Запуск обработчика сообщений");
        init();
        try {
            while (true) {
                ConsumerRecords<Void, SensorsSnapshotAvro> records = snapshotConsumer.poll(Duration.ofMillis(500));
                if (!records.isEmpty()) {
                    log.debug("Было получено {} сообщений}", records.count());
                }
                handleRecords(records);
            }
        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки снимка сенсоров", e);
        } finally {
            closeResources();
        }
    }

    private void handleRecords(ConsumerRecords<Void, SensorsSnapshotAvro> records) {
        for (ConsumerRecord<Void, SensorsSnapshotAvro> record : records) {
            SensorsSnapshotAvro sensorsSnapshot = record.value();
            log.info("Начинаю обработку снимка сенсоров: {}", sensorsSnapshot);

            List<DeviceActionRequestProto> responses = snapshotService.prepareActionRequests(sensorsSnapshot);

            log.info("В процессе анализа снимка сенсоров было создано {} ответных событий для датчиков", responses.size());

            if (!responses.isEmpty()) {
                responses.forEach(actionProducer::sendAction);
            } else {
                log.info("В процессе анализа снимка сенсоров не было создано ни одного ответного события для датчиков");
            }

            log.info("------------------------------------------------------");
        }
    }

    private void init() {
        try {
            this.snapshotConsumer = new KafkaConsumer<>(consumerConfig.getProperties());
            snapshotConsumer.subscribe(List.of(consumerConfig.getTopic()));
            log.debug("Подписка на топик {} выполнена", consumerConfig.getTopic());
        } catch (Exception e) {
            log.error("Ошибка инициализации Kafka-клиентов: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void closeResources() {
        snapshotConsumer.commitSync();

        snapshotConsumer.close();
        log.info("Ресурсы обработчика сенсоров снапшота успешно закрыты");
    }
}
