package com.example.filesummarizer.exception;


/**
 * Thrown when a file cannot be processed (e.g., corrupt file, extraction failure).
 */
public class FileProcessingException extends RuntimeException {


    public FileProcessingException(String message) {
        super(message);
    }


    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
