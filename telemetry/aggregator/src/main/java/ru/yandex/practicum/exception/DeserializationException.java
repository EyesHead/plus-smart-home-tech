package ru.yandex.practicum.exception;

public class DeserializationException extends RuntimeException {
    public DeserializationException(String s, Exception e) {
        super(s, e);
    }
}