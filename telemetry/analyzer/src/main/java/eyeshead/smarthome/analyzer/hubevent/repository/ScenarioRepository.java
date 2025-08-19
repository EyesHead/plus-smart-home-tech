package eyeshead.smarthome.analyzer.hubevent.repository;

import eyeshead.smarthome.analyzer.hubevent.model.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, String> {
    List<Scenario> findByHubId(String hubId);
    Optional<Scenario> findByHubIdAndName(String hubId, String name);

    void deleteByHubIdAndName(String hubId, String name);

    boolean existsByHubIdAndName(String hubId, String name);
}
