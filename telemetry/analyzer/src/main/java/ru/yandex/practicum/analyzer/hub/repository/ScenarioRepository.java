package ru.yandex.practicum.analyzer.hub.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.analyzer.hub.model.Scenario;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScenarioRepository extends CrudRepository<Scenario, String> {
    List<Scenario> findByHubId(String hubId);
    Optional<Scenario> findByHubIdAndName(String hubId, String name);

    void deleteByName(String name);
}
