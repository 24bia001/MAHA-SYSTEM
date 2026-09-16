package com.codelearn.houseselling.repository;

import com.codelearn.houseselling.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndCustomerIdNot(
            String email,
            Long customerId
    );

    Optional<Customer> findByEmail(String email);
}