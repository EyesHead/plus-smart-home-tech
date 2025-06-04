package eyeshead.smarthome.analyzer.hubevent.repository;

import eyeshead.smarthome.analyzer.hubevent.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, String> {
}
