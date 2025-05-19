package ru.yandex.practicum.analyzer.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.analyzer.event.model.Sensor;

public interface SensorRepository extends JpaRepository<Sensor, String> {
}
