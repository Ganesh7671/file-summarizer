package com.example.filesummarizer.service;




import com.example.filesummarizer.exception.FileProcessingException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;


/**
 * Handles saving files to the local filesystem.
 * Files are stored in the directory configured in application.properties.
 */
@Service
public class FileStorageService {


    @Value("${app.upload.dir}")
    private String uploadDir;


    /**
     * Creates the upload directory on startup if it doesn't exist.
     * @PostConstruct runs once after the bean is initialized.
     */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
            System.out.println("📁 Upload directory ready: " + uploadDir);
        } catch (IOException e) {
            throw new FileProcessingException("Could not create upload directory: " + uploadDir, e);
        }
    }


    /**
     * Saves a multipart file to disk with a unique name to avoid collisions.
     *
     * @param file The uploaded file
     * @return Absolute path of the saved file
     */
    public String saveFile(MultipartFile file) {
        try {
            // Generate unique filename: UUID + original name
            String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = Paths.get(uploadDir).resolve(uniqueFileName).toAbsolutePath();


            // Copy the file bytes to the target path
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);


            System.out.println("💾 File saved: " + targetPath);
            return targetPath.toString();


        } catch (IOException e) {
            throw new FileProcessingException("Failed to save file: " + file.getOriginalFilename(), e);
        }
    }


    /**
     * Deletes a file from the filesystem.
     *
     * @param filePath Absolute path of the file to delete
     */
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
            System.out.println("🗑️ File deleted: " + filePath);
        } catch (IOException e) {
            // Log but don't fail — the DB record can still be removed
            System.err.println("Warning: Could not delete file at " + filePath + ": " + e.getMessage());
        }
    }


    /**
     * Reads a file from disk as a byte array (used for serving downloads).
     */
    public byte[] readFile(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new FileProcessingException("Failed to read file: " + filePath, e);
        }
    }
}

