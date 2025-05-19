package ru.yandex.practicum.analyzer.snapshot;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
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
@RequiredArgsConstructor
public class SnapshotProcessor {
    private Consumer<Void, SensorsSnapshotAvro> snapshotConsumer;

    private final SnapshotRequestService snapshotService;
    private final SnapshotsConsumerConfig consumerConfig;
    private final ActionProducerService actionProducer;

    public void start() {
        log.info("СНАПШОТ-ПРОЦЕССОР ЗАПУЩЕН. Начинаем цикл ожидания сообщений из Kafka");
        try {
            while (true) {
                log.trace("poll() ожидает новые сообщения...");
                ConsumerRecords<Void, SensorsSnapshotAvro> records = snapshotConsumer.poll(Duration.ofMillis(500));

                if (records.isEmpty()) {
                    log.trace("poll() не получил сообщений");
                } else {
                    log.info("poll() получил {} сообщений", records.count());
                }

                handleRecords(records);
            }
        } catch (WakeupException ignored) {
            log.info("⚠WakeupException — остановка по инициативе");
        } catch (Exception e) {
            log.error("Ошибка во время обработки снимка сенсоров", e);
        } finally {
            closeResources();
        }
    }

    @PostConstruct
    private void init() {
        try {
            log.info("Инициализация Kafka consumer для снапшотов...");
            this.snapshotConsumer = new KafkaConsumer<>(consumerConfig.getProperties());
            snapshotConsumer.subscribe(List.of(consumerConfig.getTopic()));
            log.info("Подписка на топик '{}' выполнена", consumerConfig.getTopic());
        } catch (Exception e) {
            log.error("Ошибка инициализации Kafka-клиентов: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void handleRecords(ConsumerRecords<Void, SensorsSnapshotAvro> records) {
        for (ConsumerRecord<Void, SensorsSnapshotAvro> record : records) {
            SensorsSnapshotAvro sensorsSnapshot = record.value();
            log.info("Начинаю обработку снимка сенсоров от hub '{}'", sensorsSnapshot.getHubId());

            List<DeviceActionRequest> deviceActions = snapshotService.prepareDeviceActions(sensorsSnapshot);

            if (!deviceActions.isEmpty()) {
                log.info("Было создано {} действий, отправляем их по gRPC", deviceActions.size());
                deviceActions.forEach(deviceAction -> {
                    log.debug("Отправка DeviceAction: {}", deviceAction);
                    actionProducer.handleDeviceAction(deviceAction);
                });
            } else {
                log.info("Никакие сценарии не сработали — действий нет");
            }
            log.info("------------------------------------------------------");
        }
    }

    private void closeResources() {
        log.info("Закрытие ресурсов snapshot-процессора...");
        try {
            snapshotConsumer.commitSync();
        } catch (Exception e) {
            log.warn("Ошибка при commitSync: {}", e.getMessage());
        }
        snapshotConsumer.close();
        log.info("Kafka consumer успешно закрыт");
    }
}
