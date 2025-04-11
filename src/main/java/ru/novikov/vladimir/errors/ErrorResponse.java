package ru.novikov.vladimir.errors;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ErrorResponse {

    private final String error;
    private String description;
    private HttpStatus status;
    private Map<String, String> errors = new HashMap<>();

    public ErrorResponse(String error, String description, HttpStatus status) {
        this.error = error;
        this.description = description;
        this.status = status;
    }

    public ErrorResponse(String error, Map<String, String> errors, HttpStatus status) {
        this.error = error;
        this.description = "Some fields are incorrect";
        this.errors = errors;
        this.status = status;
    }
}