package com.codelearn.houseselling.controller;

import com.codelearn.houseselling.dto.DocumentRequest;
import com.codelearn.houseselling.dto.DocumentResponse;
import com.codelearn.houseselling.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(
            DocumentService documentService) {

        this.documentService =
                documentService;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse>
    createDocument(
            @Valid @RequestBody
            DocumentRequest request) {

        DocumentResponse response =
                documentService.createDocument(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>>
    getAllDocuments() {

        return ResponseEntity.ok(
                documentService.getAllDocuments()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse>
    getDocumentById(
            @PathVariable Long id) {

        DocumentResponse response =
                documentService
                        .getDocumentById(id);

        if (response == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse>
    updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody
            DocumentRequest request) {

        DocumentResponse response =
                documentService.updateDocument(
                        id,
                        request
                );

        if (response == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteDocument(
            @PathVariable Long id) {

        boolean deleted =
                documentService.deleteDocument(id);

        if (!deleted) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity
                .noContent()
                .build();
    }
}