package com.codelearn.houseselling.repository;

import com.codelearn.houseselling.entity.Management;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManagementRepository
        extends JpaRepository<Management, Long> {

    boolean existsByEmail(String email);

    Optional<Management> findByEmail(String email);
}