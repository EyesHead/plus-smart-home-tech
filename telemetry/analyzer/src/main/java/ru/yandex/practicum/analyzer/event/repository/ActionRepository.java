package ru.yandex.practicum.analyzer.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.analyzer.event.model.Action;

public interface ActionRepository extends JpaRepository<Action, Long> {
}
