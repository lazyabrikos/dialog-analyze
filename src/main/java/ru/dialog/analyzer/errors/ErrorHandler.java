package ru.dialog.analyzer.errors;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.dialog.analyzer.exceptions.EmptyAnswerException;
import ru.dialog.analyzer.exceptions.HttpException;
import ru.dialog.analyzer.exceptions.NotAvailableException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleServerErrors(final Exception e) {
        log.error("Получен статус 500 Internal server error {}", e.getMessage(), e);
        return new ErrorResponse("Something went wrong", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMismatchType(final MethodArgumentTypeMismatchException e) {
        log.error("Получен статус 400 Bad request {}", e.getMessage(), e);
        return new ErrorResponse("Bad request", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleJsonError(final JsonProcessingException e) {
        log.error("Response не смог быть преобразован в JSON {}",
                e.getMessage(), e);
        return new ErrorResponse("Ошибка при обработке JSON", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleHttpException(final HttpException e) {
        log.error("Got error while requesting ChatGPT API. {}", e.getMessage());
        return new ErrorResponse("Error requesting ChatGPT API", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleNotAvailableApi(final NotAvailableException e) {
        log.error("ChatGPT API not available, {}", e.getMessage());
        return new ErrorResponse("Not available", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleEmptyAnswer(final EmptyAnswerException e) {
        log.error("ChatGPT API returned empty answer after 5 retries");
        return new ErrorResponse("ChatGPT error", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

