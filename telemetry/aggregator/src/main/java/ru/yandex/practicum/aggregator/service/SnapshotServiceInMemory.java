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
        SensorsSnapshotAvro snapshot = snapshotRepository.getById(event.getHubId())
                .orElseGet(() -> snapshotRepository.save(SnapshotMapper.mapToNewSnapshot(event)));

        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());

        // Если состояние отсутствует, добавляем его
        if (oldState == null) {
            Map<String, SensorStateAvro> newStates = new HashMap<>(snapshot.getSensorsState());
            newStates.put(event.getId(), SnapshotMapper.mapToState(event));
            snapshot = SensorsSnapshotAvro.newBuilder(snapshot)
                    .setSensorsState(newStates)
                    .setTimestamp(event.getTimestamp())
                    .build();
            snapshot = snapshotRepository.save(snapshot);
            return Optional.of(snapshot);
        }

        // Проверка актуальности данных
        boolean isEventOutdated = event.getTimestamp().isBefore(oldState.getTimestamp());
        boolean isDataSame = SensorDataComparator.isEqual(oldState.getData(), event.getPayload());

        if (isEventOutdated || isDataSame) {
            return Optional.empty();
        }

        // Обновление существующего состояния
        Map<String, SensorStateAvro> newStates = new HashMap<>(snapshot.getSensorsState());
        newStates.put(event.getId(), SnapshotMapper.mapToState(event));
        SensorsSnapshotAvro newSnapshot = SensorsSnapshotAvro.newBuilder(snapshot)
                .setSensorsState(newStates)
                .setTimestamp(event.getTimestamp())
                .build();
        newSnapshot = snapshotRepository.save(newSnapshot);
        return Optional.of(newSnapshot);
    }
}
