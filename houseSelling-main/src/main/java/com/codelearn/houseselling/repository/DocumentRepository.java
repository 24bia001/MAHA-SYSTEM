package com.codelearn.houseselling.repository;

import com.codelearn.houseselling.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    boolean existsByHouseHouseIdAndDocumentNumber(
            Long houseId,
            String documentNumber
    );

    boolean existsByHouseHouseIdAndDocumentNumberAndDocumentIdNot(
            Long houseId,
            String documentNumber,
            Long documentId
    );

    // SELLER
    List<Document> findByHouseSellerSellerId(
            Long sellerId
    );

    Optional<Document> findByDocumentIdAndHouseSellerSellerId(
            Long documentId,
            Long sellerId
    );

    // CUSTOMER
    List<Document> findByHouseHouseIdIn(
            List<Long> houseIds
    );
}