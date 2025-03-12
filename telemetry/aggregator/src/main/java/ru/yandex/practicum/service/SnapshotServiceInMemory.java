package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.mapper.SnapshotMapper;
import ru.yandex.practicum.repository.SnapshotRepository;

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
        SensorsSnapshotAvro snapshot = snapshotRepository.getById(event.getHubId())
                .orElseGet(() -> snapshotRepository.save(SnapshotMapper.mapToSnapshot(event)));

        // Получаем текущее состояние датчика (если есть)
        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());

        // Проверяем, нужно ли обновлять состояние
        if (oldState != null) {
            boolean isEventOutdated = event.getTimestamp().isBefore(oldState.getTimestamp());
            boolean isDataSame = oldState.getData().equals(event.getPayload());

            if (isEventOutdated || isDataSame) {
                return Optional.empty(); // Не обновляем
            }
        }

        // Обновляем состояние (даже если oldState == null)
        Map<String, SensorStateAvro> newStates = new HashMap<>(snapshot.getSensorsState());
        newStates.put(event.getId(), SnapshotMapper.mapToState(event));
        snapshot.setSensorsState(newStates);
        snapshot.setTimestamp(event.getTimestamp());

        return Optional.of(snapshotRepository.update(snapshot));
    }
}
