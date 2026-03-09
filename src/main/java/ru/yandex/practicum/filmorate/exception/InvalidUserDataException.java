package ru.yandex.practicum.filmorate.exception;

public class InvalidUserDataException extends RuntimeException {
  public InvalidUserDataException(String message) {
    super(message);
  }
}
