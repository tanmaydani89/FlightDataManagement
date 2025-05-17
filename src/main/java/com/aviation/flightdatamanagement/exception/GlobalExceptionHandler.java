package com.aviation.flightdatamanagement.exception;

import org.slf4j.Logger; // Import SLF4J Logger
import org.slf4j.LoggerFactory; // Import SLF4J LoggerFactory
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        Map<String, String> body = new HashMap<>();
        body.put("message", ex.getMessage());
        logger.warn("Resource not found for request [{}]: {}", request.getDescription(false), ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        logger.warn("Validation failed for request [{}]: {}", request.getDescription(false), errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<?> handleDateTimeParseException(DateTimeParseException ex, WebRequest request) {
        Map<String, String> body = new HashMap<>();
        String errorMessage = STR."Invalid date-time format: \{ex.getParsedString()}. Please use ISO_DATE_TIME format (e.g., 2023-10-27T10:15:30Z).";
        body.put("message", errorMessage);
        logger.warn("DateTimeParseException for request [{}]: {}", request.getDescription(false), errorMessage, ex); // Log with exception for stack trace
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        Map<String, String> body = new HashMap<>();
        body.put("message", ex.getMessage());
        logger.warn("IllegalArgumentException for request [{}]: {}", request.getDescription(false), ex.getMessage(), ex); // Log with exception for stack trace
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CrazySupplierApiException.class) // Assuming you created this from the previous example
    public ResponseEntity<?> handleCrazySupplierApiException(CrazySupplierApiException ex, WebRequest request) {
        Map<String, String> body = new HashMap<>();
        // For external API errors, you might want a more generic message to the client
        // and log the detailed error from ex.getMessage() or ex.getCause()
        body.put("message", "Error communicating with an external flight supplier. Please try again later.");
        logger.error(
                "CrazySupplierAPIException encountered for request [{}]: {}",
                request.getDescription(false),
                ex.getMessage(),
                ex
        );
        return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> globalExceptionHandler(Exception ex, WebRequest request) {
        logger.error(
                "An unexpected error occurred while processing request [{}]: {}",
                request.getDescription(false),
                ex.getMessage(),
                ex
        );

        Map<String, String> body = new HashMap<>();
        // For unexpected errors (HttpStatus.INTERNAL_SERVER_ERROR),
        body.put("message", "An unexpected internal server error occurred. Please try again later.");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}