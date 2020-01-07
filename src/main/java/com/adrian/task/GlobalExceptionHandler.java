package com.adrian.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ConnectorException.class)
    public ResponseEntity handleServiceException(ConnectorException ex) {
        ErrorMessage errorMessage = ErrorMessage.valueOf(ex.getMessage());
        LOGGER.error(ex.getMessage());
        return new ResponseEntity<>(new ErrorFormat("ERROR", errorMessage.getMessage()), errorMessage.getHttpStatus());
    }

    @ExceptionHandler
    public ResponseEntity handleConnectionException(Exception ex) {
        LOGGER.error(ex.getMessage());
        return new ResponseEntity<>(new ErrorFormat("ERROR", "Failed during fetching data"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

class ErrorFormat {

    String message;
    String details;

    public ErrorFormat(String message, String details) {
        this.message = message;
        this.details = details;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }
}
