package ru.yandex.practicum.analyzer.event.factory;

public interface ScenarioComponentFactory<T, R> {
    T create(R request);
}