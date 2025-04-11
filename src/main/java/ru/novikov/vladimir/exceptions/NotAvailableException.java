package ru.novikov.vladimir.exceptions;

public class NotAvailableException extends RuntimeException {
  public NotAvailableException(String message) {
    super(message);
  }
}
