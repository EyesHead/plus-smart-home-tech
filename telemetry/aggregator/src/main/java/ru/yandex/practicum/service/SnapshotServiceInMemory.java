package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.mapper.SnapshotMapper;
import ru.yandex.practicum.repository.SnapshotRepository;
import ru.yandex.practicum.utils.SensorDataComparator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SnapshotServiceInMemory implements SnapshotService {
    private final SnapshotRepository snapshotRepository;

    @Override
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        // Получаем или создаем снапшот для hubId
        SensorsSnapshotAvro oldSnapshot = snapshotRepository.getById(event.getHubId())
                .orElseGet(() -> snapshotRepository.save(SnapshotMapper.mapToSnapshot(event)));

        // Получаем текущее состояние датчика (если есть)
        SensorStateAvro oldState = oldSnapshot.getSensorsState().get(event.getId());

        // Проверяем, нужно ли обновлять состояние
        if (oldState != null) {
            boolean isEventOutdated = event.getTimestamp().isBefore(oldState.getTimestamp());
            boolean isDataSame = SensorDataComparator.isEqual(oldState.getData(), event.getPayload());

            if (isEventOutdated || isDataSame) {
                return Optional.empty(); // Не обновляем
            }
        }

        // Обновляем состояние (даже если oldState == null)
        Map<String, SensorStateAvro> newStates = new HashMap<>(oldSnapshot.getSensorsState());
        newStates.put(event.getId(), SnapshotMapper.mapToState(event));
        oldSnapshot.setSensorsState(newStates);
        oldSnapshot.setTimestamp(event.getTimestamp());

        SensorsSnapshotAvro newSnapshot = snapshotRepository.update(oldSnapshot);

        return Optional.of(newSnapshot);
    }
}
