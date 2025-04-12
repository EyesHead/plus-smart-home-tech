package ru.yandex.practicum.analyzer.snapshot;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.snapshot.kafka.config.SnapshotsConsumerConfig;
import ru.yandex.practicum.analyzer.snapshot.service.ActionProducerService;
import ru.yandex.practicum.analyzer.snapshot.service.SnapshotRequestService;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class SnapshotProcessor {
    private Consumer<Void, SensorsSnapshotAvro> snapshotConsumer;

    private final SnapshotRequestService snapshotService;
    private final SnapshotsConsumerConfig consumerConfig;
    private final ActionProducerService actionProducer;

    @Autowired
    public SnapshotProcessor(ActionProducerService actionProducer,
                             SnapshotsConsumerConfig consumerConfig,
                             SnapshotRequestService snapshotService) {
        this.actionProducer = actionProducer;
        this.consumerConfig = consumerConfig;
        this.snapshotService = snapshotService;
        initConsumer();
    }

    public void start() {
        try {
            while (true) {
                ConsumerRecords<Void, SensorsSnapshotAvro> records = snapshotConsumer.poll(Duration.ofMillis(500));
                if (!records.isEmpty()) {
                    log.debug("Из топика {} было получено {} сообщений", consumerConfig.getTopic(), records.count());
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

    private void initConsumer() {
        try {
            this.snapshotConsumer = new KafkaConsumer<>(consumerConfig.getProperties());
            snapshotConsumer.subscribe(List.of(consumerConfig.getTopic()));
            log.debug("Подписка на топик {} выполнена", consumerConfig.getTopic());
        } catch (Exception e) {
            log.error("Ошибка инициализации Kafka-клиентов: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void handleRecords(ConsumerRecords<Void, SensorsSnapshotAvro> records) {
        for (ConsumerRecord<Void, SensorsSnapshotAvro> record : records) {
            SensorsSnapshotAvro sensorsSnapshot = record.value();
            log.info("Начинаю обработку снимка сенсоров: {}", sensorsSnapshot);

            List<DeviceActionRequest> deviceActions = snapshotService.prepareDeviceActions(sensorsSnapshot);

            log.info("В процессе анализа снимка сенсоров было создано {} событий для датчиков", deviceActions.size());

            if (!deviceActions.isEmpty()) {
                deviceActions.forEach(deviceAction -> {
                    log.info("Событие для датчика готово к отправке по gRPC: {}", deviceAction);
                    actionProducer.handleDeviceAction(deviceAction);
                });
            } else {
                log.info("В процессе анализа снимка сенсоров не было создано ни одного ответного события для датчиков");
            }

            log.info("------------------------------------------------------");
        }
    }

    private void closeResources() {
        snapshotConsumer.commitSync();

        snapshotConsumer.close();
        log.info("Ресурсы обработчика сенсоров снапшота успешно закрыты");
    }
}
