
package com.example.filesummarizer.repository;




import com.example.filesummarizer.model.FileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


import java.util.List;


/**
 * Spring Data MongoDB repository for FileDocument.
 * All basic CRUD operations are automatically provided.
 */
@Repository
public interface FileDocumentRepository extends MongoRepository<FileDocument, String> {


    /**
     * Find all documents ordered by upload time descending.
     * Spring Data creates the query automatically from the method name.
     */
    List<FileDocument> findAllByOrderByUploadedAtDesc();
}
