package ru.yandex.practicum.filmorate.exceptions;

public class InternalDatabaseException extends RuntimeException {
    public InternalDatabaseException(String message) {
        super(message);
    }
}
