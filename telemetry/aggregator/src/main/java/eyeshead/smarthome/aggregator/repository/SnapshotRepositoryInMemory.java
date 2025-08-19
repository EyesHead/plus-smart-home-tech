package eyeshead.smarthome.aggregator.repository;

import eyeshead.smarthome.kafka.telemetry.event.SensorsSnapshotAvro;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MVP реализация репозитория для хранения снэпшотов сенсоров каждого хаба.
 */
@Repository
public class SnapshotRepositoryInMemory implements SnapshotRepository {
    /**
     * MVP хранилище в памяти.
     *
     * @implNote Key - Hub id снэпшота, Value - снэпшот с информацией о сенсорах
     */

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    @Override
    public Optional<SensorsSnapshotAvro> getById(String hubId) {
        return Optional.ofNullable(snapshots.get(hubId));
    }

    @Override
    public SensorsSnapshotAvro save(SensorsSnapshotAvro snapshot) {
        snapshots.put(snapshot.getHubId(), snapshot);
        return snapshot; // Важно вернуть сохраненный объект!
    }
}
