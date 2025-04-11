package ru.novikov.vladimir.exceptions;

public class HttpException extends RuntimeException {
    public HttpException(String message) {
        super(message);
    }
}
