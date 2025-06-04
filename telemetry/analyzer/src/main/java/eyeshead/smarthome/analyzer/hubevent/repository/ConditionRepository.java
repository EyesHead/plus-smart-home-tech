package eyeshead.smarthome.analyzer.hubevent.repository;

import eyeshead.smarthome.analyzer.hubevent.model.Condition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionRepository extends JpaRepository<Condition, Long> {
}
