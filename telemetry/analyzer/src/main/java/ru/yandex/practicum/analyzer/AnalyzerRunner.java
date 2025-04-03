package ru.yandex.practicum.analyzer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.hub.HubEventProcessor;
import ru.yandex.practicum.analyzer.snapshot.SnapshotProcessor;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {
    HubEventProcessor hubEventProcessor;
    SnapshotProcessor snapshotProcessor;

    @Override
    public void run(String... args) throws Exception {
        // запускаем в отдельном потоке обработчик событий
        // от пользовательских хабов
        Thread hubEventsThread = new Thread(hubEventProcessor);
        hubEventsThread.setName("HubEventHandlerThread");
        hubEventsThread.start();

        // В текущем потоке начинаем обработку
        // снимков состояния датчиков
        snapshotProcessor.start();
    }
}
