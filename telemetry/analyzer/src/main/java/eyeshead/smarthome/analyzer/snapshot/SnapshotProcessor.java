package eyeshead.smarthome.analyzer.snapshot;

import eyeshead.smarthome.analyzer.snapshot.kafka.config.SnapshotsConsumerConfig;
import eyeshead.smarthome.analyzer.snapshot.service.GrpcActionProducerService;
import eyeshead.smarthome.analyzer.snapshot.service.SnapshotRequestService;
import eyeshead.smarthome.grpc.telemetry.event.DeviceActionRequest;
import eyeshead.smarthome.kafka.telemetry.event.SensorsSnapshotAvro;
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

/**
 * SnapshotProcessor — основной компонент, отвечающий за:
 * <ul>
 *   <li>Получение данных из Kafka (топик {@code telemetry.snapshots.v1})</li>
 *   <li>Анализ полученных snapshot-ов сенсоров</li>
 *   <li>Отправку команд ({@code DeviceActionRequest}) в сервис hub-router по gRPC</li>
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
 *   <li>Использует {@code KafkaConsumer}, подписанный на один топик</li>
 *   <li>Работает в основном потоке, gRPC-вызовы могут блокировать его при долгом ожидании ответа</li>
 *   <li>Обработка снапшотов синхронная, что может привести к задержкам, если gRPC-сервис недоступен</li>
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
    private final SnapshotsConsumerConfig consumerConfig;
    private final GrpcActionProducerService grpcActionService;

    public void start() {
        log.info("Обработчик сообщений запущен");
        try {
            while (true) {
                ConsumerRecords<Void, SensorsSnapshotAvro> records = consumer.poll(consumerConfig.getPollTimeout());

                if (!records.isEmpty()) {
                    log.debug("Из топика {} было получено {} сообщений}", consumerConfig.getTopic(), records.count());
                    handleRecords(records);
                    log.info("------------------------------------------------");
                }
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
            this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
            consumer.subscribe(List.of(consumerConfig.getTopic()));
            log.info("Подписка на топик '{}' выполнена", consumerConfig.getTopic());
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
                deviceActions.forEach(deviceAction -> {
                    log.info("Отправка DeviceAction в hub-router");
                    grpcActionService.handleDeviceAction(deviceAction);
                });
            } else {
                log.info("Никакие сценарии не сработали — действий нет");
            }
            consumer.commitSync();
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
