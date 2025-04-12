package ru.yandex.practicum.analyzer.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.analyzer.event.model.Condition;

public interface ConditionRepository extends JpaRepository<Condition, Long> {
}
