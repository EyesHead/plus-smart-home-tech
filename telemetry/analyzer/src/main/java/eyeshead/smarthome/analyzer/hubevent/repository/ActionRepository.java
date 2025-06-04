package eyeshead.smarthome.analyzer.hubevent.repository;

import eyeshead.smarthome.analyzer.hubevent.model.Action;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionRepository extends JpaRepository<Action, Long> {
}
