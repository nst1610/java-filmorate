package ru.yandex.practicum.filmorate.exception;

public class InvalidFilmDataException extends RuntimeException {
    public InvalidFilmDataException(String message) {
        super(message);
    }
}
