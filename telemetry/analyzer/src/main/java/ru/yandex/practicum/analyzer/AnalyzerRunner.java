package ru.yandex.practicum.analyzer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.event.HubEventProcessor;
import ru.yandex.practicum.analyzer.snapshot.SnapshotProcessor;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class AnalyzerRunner implements CommandLineRunner {
    HubEventProcessor hubEventProcessor;
    SnapshotProcessor snapshotProcessor;

    @Override
    public void run(String... args) {
        log.info("AnalyzerRunner запускается");

        Thread hubEventsThread = new Thread(hubEventProcessor);
        hubEventsThread.setName("HubEventHandlerThread");
        hubEventsThread.start();
        log.info("HubEventProcessor запущен в отдельном потоке");

        // ❗ Важно: snapshotProcessor.start() блокирует поток, убедись, что это ожидаемо
        log.info("Запускаю SnapshotProcessor в текущем потоке (будет заблокирован)");
        snapshotProcessor.start();

        // Этот лог никогда не дойдёт, так как start() — бесконечный цикл
        log.info("Этот лог не должен быть достигнут — snapshotProcessor.start() бесконечен");
    }
}