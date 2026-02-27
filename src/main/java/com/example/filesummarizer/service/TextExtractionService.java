package com.example.filesummarizer.service;


import com.example.filesummarizer.exception.FileProcessingException;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;


import java.io.ByteArrayInputStream;
import java.io.IOException;


/**
 * Uses Apache Tika to extract plain text from uploaded documents.
 */
@Service
public class TextExtractionService {


    private final Tika tika = new Tika();
    private static final int MAX_CHARS = 4000;


    /**
     * ✅ NEW: Extracts text from raw bytes — avoids InputStream re-use issues.
     * Called from the controller after capturing file.getBytes().
     */
    public String extractTextFromBytes(byte[] fileBytes, String contentType) {
        // ✅ FIX: Never run Tika on image files — it returns binary noise
        if (contentType != null && contentType.startsWith("image/")) {
            return ""; // Images are handled by llava, not Tika
        }


        try (ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes)) {
            String text = tika.parseToString(bais);


            if (text == null || text.isBlank()) {
                return "No readable text could be extracted from this document.";
            }


            // Truncate to avoid token overflow in the AI model
            if (text.length() > MAX_CHARS) {
                text = text.substring(0, MAX_CHARS) + "... [content truncated for summarization]";
            }


            return text.trim();


        } catch (IOException | TikaException e) {
            throw new FileProcessingException("Failed to extract text from document.", e);
        }
    }


    /**
     * Returns "IMAGE" for image MIME types, "TEXT" for everything else.
     */
    public String getCategory(String contentType) {
        if (contentType == null) return "TEXT";
        return contentType.startsWith("image/") ? "IMAGE" : "TEXT";
    }


    /**
     * Validates that the uploaded file type is supported.
     */
    public void validateFileType(String contentType, String originalFilename) {
        if (contentType == null) {
            throw new IllegalArgumentException("Cannot determine file type.");
        }


        boolean isSupported =
                contentType.equals("application/pdf") ||
                        contentType.equals("application/msword") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                        contentType.equals("image/jpeg") ||
                        contentType.equals("image/png");


        if (!isSupported) {
            throw new IllegalArgumentException(
                    "Unsupported file type: " + contentType +
                            ". Accepted: PDF, DOC, DOCX, JPG, PNG");
        }
    }
}


