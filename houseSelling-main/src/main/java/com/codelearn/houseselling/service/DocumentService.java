package com.codelearn.houseselling.service;

import com.codelearn.houseselling.dto.DocumentRequest;
import com.codelearn.houseselling.dto.DocumentResponse;
import com.codelearn.houseselling.entity.Document;
import com.codelearn.houseselling.entity.House;
import com.codelearn.houseselling.entity.Seller;
import com.codelearn.houseselling.repository.DocumentRepository;
import com.codelearn.houseselling.repository.HouseRepository;
import com.codelearn.houseselling.repository.SellerRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final HouseRepository houseRepository;
    private final SellerRepository sellerRepository;

    public DocumentService(
            DocumentRepository documentRepository,
            HouseRepository houseRepository,
            SellerRepository sellerRepository) {

        this.documentRepository = documentRepository;
        this.houseRepository = houseRepository;
        this.sellerRepository = sellerRepository;
    }

    public DocumentResponse createDocument(
            DocumentRequest request) {

        Seller seller = getLoggedInSeller();

        House house =
                houseRepository
                        .findById(request.getHouseId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "House not found with id: "
                                                + request.getHouseId()
                                )
                        );

        // Seller can only create documents
        // for houses they own.
        if (house.getSeller() == null
                || !house.getSeller()
                .getSellerId()
                .equals(seller.getSellerId())) {

            throw new AccessDeniedException(
                    "You cannot create a document for another seller's house"
            );
        }

        // Same house cannot have two documents
        // with the same document number.
        if (documentRepository
                .existsByHouseHouseIdAndDocumentNumber(
                        request.getHouseId(),
                        request.getDocumentNumber()
                )) {

            throw new IllegalArgumentException(
                    "Document number already exists for house: "
                            + request.getHouseId()
            );
        }

        Document document = new Document();

        document.setDocumentName(
                request.getDocumentName()
        );

        document.setDocumentType(
                request.getDocumentType()
        );

        document.setDocumentNumber(
                request.getDocumentNumber()
        );

        document.setIssueDate(
                request.getIssueDate()
        );

        document.setStatus(
                request.getStatus()
        );

        document.setHouse(
                house
        );

        Document savedDocument =
                documentRepository.save(document);

        return convertToResponse(savedDocument);
    }

    public List<DocumentResponse> getAllDocuments() {

        Seller seller = getLoggedInSeller();

        return documentRepository
                .findByHouseSellerSellerId(
                        seller.getSellerId()
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public DocumentResponse getDocumentById(
            Long id) {

        Seller seller = getLoggedInSeller();

        Document document =
                documentRepository
                        .findByDocumentIdAndHouseSellerSellerId(
                                id,
                                seller.getSellerId()
                        )
                        .orElse(null);

        if (document == null) {
            return null;
        }

        return convertToResponse(document);
    }

    public DocumentResponse updateDocument(
            Long id,
            DocumentRequest request) {

        Seller seller = getLoggedInSeller();

        Document existingDocument =
                documentRepository
                        .findByDocumentIdAndHouseSellerSellerId(
                                id,
                                seller.getSellerId()
                        )
                        .orElse(null);

        if (existingDocument == null) {
            return null;
        }

        House house =
                houseRepository
                        .findById(request.getHouseId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "House not found with id: "
                                                + request.getHouseId()
                                )
                        );

        // Seller cannot move document
        // to another seller's house.
        if (house.getSeller() == null
                || !house.getSeller()
                .getSellerId()
                .equals(seller.getSellerId())) {

            throw new AccessDeniedException(
                    "You cannot use another seller's house"
            );
        }

        // Ignore current document when checking
        // duplicate document number.
        if (documentRepository
                .existsByHouseHouseIdAndDocumentNumberAndDocumentIdNot(
                        request.getHouseId(),
                        request.getDocumentNumber(),
                        id
                )) {

            throw new IllegalArgumentException(
                    "Document number already exists for house: "
                            + request.getHouseId()
            );
        }

        existingDocument.setDocumentName(
                request.getDocumentName()
        );

        existingDocument.setDocumentType(
                request.getDocumentType()
        );

        existingDocument.setDocumentNumber(
                request.getDocumentNumber()
        );

        existingDocument.setIssueDate(
                request.getIssueDate()
        );

        existingDocument.setStatus(
                request.getStatus()
        );

        existingDocument.setHouse(
                house
        );

        Document updatedDocument =
                documentRepository.save(
                        existingDocument
                );

        return convertToResponse(updatedDocument);
    }

    public boolean deleteDocument(
            Long id) {

        Seller seller = getLoggedInSeller();

        Document document =
                documentRepository
                        .findByDocumentIdAndHouseSellerSellerId(
                                id,
                                seller.getSellerId()
                        )
                        .orElse(null);

        if (document == null) {
            return false;
        }

        documentRepository.delete(document);

        return true;
    }

    private Seller getLoggedInSeller() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "Seller is not authenticated"
            );
        }

        String email =
                authentication.getName();

        return sellerRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Logged-in seller not found"
                        )
                );
    }

    private DocumentResponse convertToResponse(
            Document document) {

        DocumentResponse response =
                new DocumentResponse();

        response.setDocumentId(
                document.getDocumentId()
        );

        response.setDocumentName(
                document.getDocumentName()
        );

        response.setDocumentType(
                document.getDocumentType()
        );

        response.setDocumentNumber(
                document.getDocumentNumber()
        );

        response.setIssueDate(
                document.getIssueDate()
        );

        response.setStatus(
                document.getStatus()
        );

        if (document.getHouse() != null) {

            response.setHouseId(
                    document.getHouse()
                            .getHouseId()
            );

            response.setHouseTitle(
                    document.getHouse()
                            .getTitle()
            );
        }

        return response;
    }
}