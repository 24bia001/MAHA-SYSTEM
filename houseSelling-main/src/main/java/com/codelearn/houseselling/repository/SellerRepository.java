package com.codelearn.houseselling.repository;

import com.codelearn.houseselling.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndSellerIdNot(
            String email,
            Long sellerId
    );
}