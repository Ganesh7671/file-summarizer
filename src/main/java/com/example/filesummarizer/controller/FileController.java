package com.example.filesummarizer.controller;


import com.example.filesummarizer.exception.FileNotFoundException;
import com.example.filesummarizer.model.FileDocument;
import com.example.filesummarizer.repository.FileDocumentRepository;
import com.example.filesummarizer.service.AiService;
import com.example.filesummarizer.service.FileStorageService;
import com.example.filesummarizer.service.TextExtractionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "*")
public class FileController {


    private final FileStorageService fileStorageService;
    private final TextExtractionService textExtractionService;
    private final AiService aiService;
    private final FileDocumentRepository fileDocumentRepository;


    public FileController(
            FileStorageService fileStorageService,
            TextExtractionService textExtractionService,
            AiService aiService,
            FileDocumentRepository fileDocumentRepository) {
        this.fileStorageService = fileStorageService;
        this.textExtractionService = textExtractionService;
        this.aiService = aiService;
        this.fileDocumentRepository = fileDocumentRepository;
    }


    /**
     * POST /files/upload
     *
     * Key fix: for images, we call file.getBytes() BEFORE saving to disk.
     * MultipartFile holds the bytes in memory — reading them here is safe
     * and avoids re-opening the file from disk (which caused stale read issues).
     */
    @PostMapping("/upload")
    public ResponseEntity<List<FileDocument>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files) {


        List<FileDocument> results = files.stream().map(file -> {
            try {
                String contentType = file.getContentType();
                String originalFilename = file.getOriginalFilename();


                // 1. Validate type first (throws 415 if unsupported)
                textExtractionService.validateFileType(contentType, originalFilename);


                // 2. Determine category
                String category = textExtractionService.getCategory(contentType);


                // 3. ✅ FIX: Capture bytes from MultipartFile BEFORE saving.
                //    file.getInputStream() can only be read once — Tika consumes it.
                //    file.getBytes() gives us a fresh copy safely.
                byte[] fileBytes = file.getBytes();


                // 4. Save file to disk
                String savedPath = fileStorageService.saveFile(file);


                // 5. Generate AI content
                String summary;
                if ("IMAGE".equals(category)) {
                    // ✅ FIX: Pass the in-memory bytes directly — no disk re-read needed
                    summary = aiService.describeImage(fileBytes);
                } else {
                    // For documents: extract text from the bytes, then summarize
                    String extractedText = textExtractionService.extractTextFromBytes(fileBytes, contentType);
                    summary = aiService.summarizeText(extractedText);
                }


                // 6. Persist metadata to MongoDB
                FileDocument doc = FileDocument.builder()
                        .fileName(originalFilename)
                        .fileType(contentType)
                        .filePath(savedPath)
                        .fileSize(file.getSize())
                        .summary(summary)
                        .uploadedAt(LocalDateTime.now())
                        .category(category)
                        .build();


                return fileDocumentRepository.save(doc);


            } catch (IOException e) {
                throw new com.example.filesummarizer.exception.FileProcessingException(
                        "Failed to read file bytes: " + file.getOriginalFilename(), e);
            }
        }).toList();


        return ResponseEntity.ok(results);
    }


    @GetMapping
    public ResponseEntity<List<FileDocument>> getAllFiles() {
        return ResponseEntity.ok(fileDocumentRepository.findAllByOrderByUploadedAtDesc());
    }


    @GetMapping("/{id}")
    public ResponseEntity<FileDocument> getFileById(@PathVariable String id) {
        FileDocument doc = fileDocumentRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(id));
        return ResponseEntity.ok(doc);
    }


    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String id) {
        FileDocument doc = fileDocumentRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(id));


        byte[] fileBytes = fileStorageService.readFile(doc.getFilePath());


        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(doc.getFileType()))
                .body(fileBytes);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        FileDocument doc = fileDocumentRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(id));
        fileStorageService.deleteFile(doc.getFilePath());
        fileDocumentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}



