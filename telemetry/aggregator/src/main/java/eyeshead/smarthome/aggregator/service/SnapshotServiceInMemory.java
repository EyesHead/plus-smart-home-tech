package eyeshead.smarthome.aggregator.service;

import eyeshead.smarthome.aggregator.repository.SnapshotRepository;
import eyeshead.smarthome.kafka.telemetry.event.SensorEventAvro;
import eyeshead.smarthome.kafka.telemetry.event.SensorStateAvro;
import eyeshead.smarthome.kafka.telemetry.event.SensorsSnapshotAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
            Optional<SensorsSnapshotAvro> newSnapshot = createNewSnapshot(event);
            log.info("Снапшота с hubId = {} ещё нет. Создаем новый snapshot с одним показанием сенсора:\n{}",
                    event.getHubId(), newSnapshot);
            return newSnapshot;
        }

        SensorsSnapshotAvro snapshot = snapshotOpt.get();
        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());
        // Если событие добавлялось ранее в snapshot
        if (oldState != null) {
            log.debug("Были получены данные на уже существующий сенсор с id = {}. Старое/новое состояние сенсора :\n{}\n{}",
                    event.getId(), oldState.getData(), event.getPayload());
            boolean isEventOutdated = oldState.getTimestamp().isAfter(event.getTimestamp());
            boolean isDataSame = SensorDataComparator.isEqual(oldState.getData(), event.getPayload());
            if (isEventOutdated || isDataSame) {
                log.warn("Новые полученные данные неактуальны. Возврат Optional.empty()");
                return Optional.empty();
            } else {
                // Удаляем старые показания сенсора, т.к. далее мы их обновим
                snapshot.getSensorsState().remove(event.getId());
            }
        }
        log.info("Обновляем показания сенсора с id = {} в снапшоте с hubId = {}", event.getId(), snapshot.getHubId());
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
