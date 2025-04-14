package ru.dialog.analyzer.exceptions;

public class EmptyAnswerException extends RuntimeException {
  public EmptyAnswerException(String message) {
    super(message);
  }
}
