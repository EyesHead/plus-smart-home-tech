package ru.yandex.practicum.aggregator.exception;

public class DeserializationException extends RuntimeException {
    public DeserializationException(String s, Exception e) {
        super(s, e);
    }
}