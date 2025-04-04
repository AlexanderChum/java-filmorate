package ru.yandex.practicum.filmorate.exceptions;

public class DbDeletionException extends RuntimeException {
    public DbDeletionException(String message) {
        super(message);
    }
}
