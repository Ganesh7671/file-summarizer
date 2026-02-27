package com.example.filesummarizer;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Main entry point for the File Summarizer application.
 * Starts the embedded Tomcat server on port 8090.
 */
@SpringBootApplication
public class FileSummarizerApplication {


    public static void main(String[] args) {
        SpringApplication.run(FileSummarizerApplication.class, args);
        System.out.println("✅ File Summarizer running at http://localhost:8090");
    }
}
