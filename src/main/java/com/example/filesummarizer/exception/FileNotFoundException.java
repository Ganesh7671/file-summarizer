package com.example.filesummarizer.exception;


/**
 * Thrown when a requested file does not exist in the database.
 */
public class FileNotFoundException extends RuntimeException {


    public FileNotFoundException(String id) {
        super("File not found with id: " + id);
    }
}
