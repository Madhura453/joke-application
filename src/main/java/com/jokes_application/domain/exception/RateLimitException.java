package com.jokes_application.domain.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class RateLimitException extends RuntimeException{

    private static final long serialVersionUID = 1L;
    final Integer errorCode;
    final HttpStatus httpErrorCode;

    public RateLimitException(Integer errorCode, String errorMessage, HttpStatus httpErrorCode) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.httpErrorCode = httpErrorCode;
    }
}
