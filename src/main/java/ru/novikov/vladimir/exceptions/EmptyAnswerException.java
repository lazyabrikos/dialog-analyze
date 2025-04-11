package ru.novikov.vladimir.exceptions;

public class EmptyAnswerException extends RuntimeException {
  public EmptyAnswerException(String message) {
    super(message);
  }
}
