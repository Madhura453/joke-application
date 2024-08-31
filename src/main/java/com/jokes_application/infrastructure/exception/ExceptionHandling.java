package com.jokes_application.infrastructure.exception;

import com.jokes_application.domain.exception.RateLimitException;
import com.jokes_application.domain.dto.ExceptionResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ExceptionHandling {

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Object> handleMovieNotFoundException(RateLimitException exception,
                                                               WebRequest webRequest) {
        ExceptionResponseDto response = new ExceptionResponseDto();
        response.setMessage(exception.getMessage());
        response.setStatusCode(exception.getErrorCode());
        response.setDescription(webRequest.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<Object> handleExceptionsBadRequest(Throwable exception, WebRequest webRequest) {
        ExceptionResponseDto response = new ExceptionResponseDto();
        response.setMessage(exception.getMessage());
        response.setStatusCode(400);
        response.setDescription(webRequest.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
