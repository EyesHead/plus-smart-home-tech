package ru.yandex.practicum.analyzer.event.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.analyzer.event.model.Sensor;

import java.util.Collection;
import java.util.List;

public interface SensorRepository extends JpaRepository<Sensor, String> {
    @EntityGraph(attributePaths = {"type"})
    @Query("SELECT s FROM Sensor s WHERE s.id IN :ids AND s.hubId = :hubId")
    List<Sensor> findSensorsByIdsAndHubId(@Param("ids") Collection<String> ids,
                                          @Param("hubId") String hubId);
}
