package com.codelearn.houseselling.repository;

import com.codelearn.houseselling.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SaleRepository
        extends JpaRepository<Sale, Long> {

    boolean existsByHouseHouseIdAndStatus(
            Long houseId,
            String status
    );

    boolean existsByHouseHouseIdAndStatusAndSaleIdNot(
            Long houseId,
            String status,
            Long saleId
    );

    boolean existsByCustomerCustomerId(
            Long customerId
    );

    // SELLER
    List<Sale> findByHouseSellerSellerId(
            Long sellerId
    );

    Optional<Sale> findBySaleIdAndHouseSellerSellerId(
            Long saleId,
            Long sellerId
    );

    boolean existsByCustomerCustomerIdAndHouseSellerSellerId(
            Long customerId,
            Long sellerId
    );

    boolean existsByCustomerCustomerIdAndHouseSellerSellerIdNot(
            Long customerId,
            Long sellerId
    );

    // CUSTOMER
    List<Sale> findByCustomerCustomerId(
            Long customerId
    );

    List<Sale> findByCustomerCustomerIdAndStatus(
            Long customerId,
            String status
    );
}