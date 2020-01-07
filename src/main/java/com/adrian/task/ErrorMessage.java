package com.adrian.task;

import org.springframework.http.HttpStatus;

public enum ErrorMessage {

    ERROR_403("Sorry, your request cannot be processed", HttpStatus.FORBIDDEN),
    ERROR_404("Cannot find repository with given params", HttpStatus.NOT_FOUND),
    ERROR_500("Cannot connect to source api", HttpStatus.INTERNAL_SERVER_ERROR);

    private String message;
    private HttpStatus httpStatus;

    ErrorMessage(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
