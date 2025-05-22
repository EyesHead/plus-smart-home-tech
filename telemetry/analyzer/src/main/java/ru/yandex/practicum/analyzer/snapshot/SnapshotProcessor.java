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
import ru.yandex.practicum.analyzer.snapshot.service.GrpcActionProducerService;
import ru.yandex.practicum.analyzer.snapshot.service.SnapshotRequestService;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;

/**
 * SnapshotProcessor — основной компонент, отвечающий за:
 * <ul>
 *   <li>получение данных из Kafka (топик {@code telemetry.snapshots.v1});</li>
 *   <li>анализ полученных snapshot-ов сенсоров;</li>
 *   <li>отправку команд ({@code DeviceActionRequest}) в сервис hub-router по gRPC;</li>
 * </ul>
 *
 * <p><b>Поток обработки:</b></p>
 *
 * <pre>
 *     Kafka Topic               SnapshotProcessor         SnapshotRequestService         GrpcActionProducerService
 *  ---------------------     -----------------------     -------------------------     ----------------------------
 *  |SensorsSnapshotAvro| --> |poll() каждые 0.5 сек| --> | prepareDeviceActions()| --> |gRPC: handleDeviceAction()|
 *  ---------------------     -----------------------     -------------------------     ----------------------------
 *                                                                \                             /
 *                                                           Если есть действия → отправка через gRPC
 * </pre>
 *
 * <p><b>Описание ключевых шагов:</b></p>
 * <ol>
 *   <li>Компонент запускается методом {@link #start()}, который запускает бесконечный цикл получения сообщений из Kafka.</li>
 *   <li>Каждые 500 мс вызывается {@code poll()}, и если получены новые {@code SensorsSnapshotAvro}, они обрабатываются.</li>
 *   <li>Обработка происходит в методе {@link #handleRecords(ConsumerRecords)} — каждый snapshot анализируется через {@link SnapshotRequestService}.</li>
 *   <li>Если в результате анализа были сформированы {@code DeviceActionRequest}, они отправляются через gRPC-клиент {@link GrpcActionProducerService}.</li>
 * </ol>
 *
 * <p><b>Особенности:</b></p>
 * <ul>
 *   <li>Использует {@code KafkaConsumer}, подписанный на один топик.</li>
 *   <li>Работает в основном потоке, gRPC-вызовы могут блокировать его при долгом ожидании ответа.</li>
 *   <li>Обработка снапшотов синхронная, что может привести к задержкам, если gRPC-сервис недоступен.</li>
 * </ul>
 *
 * <p><b>Возможные улучшения:</b></p>
 * <ul>
 *   <li>Вынести gRPC-вызовы в отдельный {@code ExecutorService}, чтобы Kafka-поток не блокировался.</li>
 *   <li>Добавить систему ретраев в gRPC-вызовах с настройкой времени и количества ретраев через конфиг-файл.</li>
 * </ul>
 *
 * @author Даниил Куксарь
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SnapshotProcessor {
    private Consumer<Void, SensorsSnapshotAvro> consumer;

    private final SnapshotRequestService service;
    private final SnapshotsConsumerConfig config;
    private final GrpcActionProducerService grpcActionService;

    /**
     * Запускает бесконечный цикл получения данных из Kafka.
     * Каждые 500 мс вызывает {@code poll()}, анализирует полученные записи и при необходимости отправляет команды через gRPC.
     */
    public void start() {
        log.info("СНАПШОТ-ПРОЦЕССОР ЗАПУЩЕН. Начинаем цикл ожидания сообщений из Kafka");
        try {
            while (true) {
                log.trace("poll() ожидает новые сообщения...");
                ConsumerRecords<Void, SensorsSnapshotAvro> records = consumer.poll(Duration.ofMillis(500));

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

    /**
     * Инициализирует Kafka consumer и подписывается на указанный в конфигурации топик.
     */
    @PostConstruct
    private void init() {
        try {
            log.info("Инициализация Kafka consumer для снапшотов...");
            this.consumer = new KafkaConsumer<>(config.getProperties());
            consumer.subscribe(List.of(config.getTopic()));
            log.info("Подписка на топик '{}' выполнена", config.getTopic());
        } catch (Exception e) {
            log.error("Ошибка инициализации Kafka-клиентов: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Обрабатывает каждую запись из списка полученных снапшотов.
     * Анализирует данные и при наличии действий отправляет их по gRPC.
     *
     * @param records список полученных Kafka записей
     */
    private void handleRecords(ConsumerRecords<Void, SensorsSnapshotAvro> records) {
        for (ConsumerRecord<Void, SensorsSnapshotAvro> record : records) {
            SensorsSnapshotAvro sensorsSnapshot = record.value();
            log.info("Начинаю обработку снимка сенсоров от hub '{}'", sensorsSnapshot.getHubId());

            List<DeviceActionRequest> deviceActions = service.prepareDeviceActions(sensorsSnapshot);

            if (!deviceActions.isEmpty()) {
                log.info("В процессе работы сервиса было создано {} событий, отправляем их по gRPC", deviceActions.size());
                deviceActions.forEach(deviceAction -> {
                    log.debug("Отправка DeviceAction в hub-router: {}", deviceAction);
                    grpcActionService.handleDeviceAction(deviceAction);
                });
            } else {
                log.info("Никакие сценарии не сработали — действий нет");
            }
            log.info("------------------------------------------------------");
        }
    }

    /**
     * Закрывает Kafka consumer и коммитит последний offset.
     */
    private void closeResources() {
        log.info("Закрытие ресурсов snapshot-процессора...");
        consumer.commitSync();
        consumer.close();
        log.info("Kafka consumer успешно закрыт");
    }
}
