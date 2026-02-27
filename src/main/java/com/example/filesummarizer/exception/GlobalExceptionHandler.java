package com.example.filesummarizer.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;


import java.time.LocalDateTime;
import java.util.Map;


/**
 * Centralized exception handler for all REST controllers.
 * @RestControllerAdvice intercepts exceptions and returns clean JSON error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * Handles file-not-found errors → 404
     */
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFileNotFound(FileNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }


    /**
     * Handles file processing failures → 422
     */
    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<Map<String, Object>> handleFileProcessing(FileProcessingException ex) {
        return buildError(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }


    /**
     * Handles files that exceed the max upload size → 413
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxSize(MaxUploadSizeExceededException ex) {
        return buildError(HttpStatus.PAYLOAD_TOO_LARGE,
                "File is too large. Maximum allowed size is 50MB.");
    }


    /**
     * Handles unsupported file formats → 415
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
    }


    /**
     * Catch-all for unexpected errors → 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + ex.getMessage());
    }


    /**
     * Helper to build a consistent error response body.
     */
    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> error = Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "timestamp", LocalDateTime.now().toString()
        );
        return ResponseEntity.status(status).body(error);
    }
}
