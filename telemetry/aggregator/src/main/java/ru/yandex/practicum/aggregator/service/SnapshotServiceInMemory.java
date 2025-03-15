package ru.yandex.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.aggregator.mapper.SnapshotMapper;
import ru.yandex.practicum.aggregator.repository.SnapshotRepository;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnapshotServiceInMemory implements SnapshotService {
    private final SnapshotRepository snapshotRepository;

    @Override
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        Optional<SensorsSnapshotAvro> snapshotOpt = snapshotRepository.getById(event.getHubId());

        if (snapshotOpt.isEmpty()) {
            return createNewSnapshot(event);
        }

        SensorsSnapshotAvro snapshot = snapshotOpt.get();
        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());
        // Если событие добавлялось ранее в snapshot
        if (oldState != null) {
            log.debug("У снапшота есть данные об этом сенсоре. Проверка актуальности данных сенсора с id = {}", event.getId());
            boolean isEventOutdated = oldState.getTimestamp().isAfter(event.getTimestamp());
            boolean isDataSame = SensorDataComparator.isEqual(oldState.getData(), event.getPayload());

            if (isEventOutdated || isDataSame) {
                log.info("Данные неактуальны. Возврат Optional.empty()");
                return Optional.empty();
            }
        }
        log.info("Были получены новые данные для snapshot с hubId = {}. Обновляем показания сенсора с id = {}",
                snapshot.getHubId(), event.getId());
        // Получены новые данные. Обновляем показания счетчиков в snapshot
        return updateSnapshotWithEventData(snapshot, event);
    }

    private Optional<SensorsSnapshotAvro> updateSnapshotWithEventData(SensorsSnapshotAvro snapshot, SensorEventAvro event) {
        Map<String, SensorStateAvro> newStates = new HashMap<>(snapshot.getSensorsState());
        newStates.put(event.getId(), SnapshotMapper.mapToState(event));
        SensorsSnapshotAvro newSnapshot = SensorsSnapshotAvro.newBuilder(snapshot)
                .setSensorsState(newStates)
                .setTimestamp(event.getTimestamp())
                .build();
        snapshotRepository.save(newSnapshot);
        return Optional.of(newSnapshot);
    }

    private Optional<SensorsSnapshotAvro> createNewSnapshot(SensorEventAvro event) {
        SensorsSnapshotAvro snapshotFromEvent = SnapshotMapper.mapToNewSnapshot(event);
        return Optional.of(snapshotRepository.save(snapshotFromEvent));
    }
}
