package com.example.filesummarizer.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;


/**
 * MongoDB document that stores metadata for each uploaded file.
 * The @Document annotation maps this class to the "file_documents" collection.
 */
@Data                   // Lombok: generates getters, setters, toString, equals, hashCode
@Builder                // Lombok: provides a builder pattern
@NoArgsConstructor      // Lombok: no-args constructor
@AllArgsConstructor     // Lombok: all-args constructor
@Document(collection = "file_documents")
public class FileDocument {


    @Id
    private String id;


    /** Original file name (e.g., "report.pdf") */
    private String fileName;


    /** MIME type (e.g., "application/pdf", "image/png") */
    private String fileType;


    /** Absolute path to the saved file on the local filesystem */
    private String filePath;


    /** Size of the file in bytes */
    private Long fileSize;


    /** AI-generated summary or image description */
    private String summary;


    /** Timestamp of when the file was uploaded */
    private LocalDateTime uploadedAt;


    /** Category: TEXT or IMAGE */
    private String category;
}

