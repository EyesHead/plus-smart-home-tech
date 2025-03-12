package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.mapper.SnapshotMapper;
import ru.yandex.practicum.repository.SnapshotRepository;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SnapshotServiceInMemory implements SnapshotService {
    private final SnapshotRepository snapshotRepository;

    @Override
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        /* Проверяем, есть ли snapshot для event.getHubId()
        Если snapshot есть, то достаём его
        Если нет, то создаём новый
        */
        SensorsSnapshotAvro snapshot = snapshotRepository.getById(event.getHubId())
                .orElse(snapshotRepository.save(
                        SnapshotMapper.mapToSnapshot(event)));

        // Проверяем, есть ли в снапшоте данные для event.getId()
        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());
        if (oldState == null ||
                oldState.getTimestamp().isAfter(event.getTimestamp()) &&
                oldState.getData().equals(event.getPayload())
        ) {
            return Optional.empty();
        }

        // если дошли до сюда, значит, пришли новые данные и
        // снапшот нужно обновить
        snapshot.setSensorsState(Map.of(event.getId(), SnapshotMapper.mapToState(event)));
        snapshot.setTimestamp(event.getTimestamp());

        return Optional.of(snapshotRepository.update(snapshot));
    }
}
