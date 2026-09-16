package com.codelearn.houseselling.repository;

import com.codelearn.houseselling.entity.House;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HouseRepository
        extends JpaRepository<House, Long> {

    boolean existsBySellerSellerId(Long sellerId);

    List<House> findBySellerSellerId(Long sellerId);

    Optional<House> findByHouseIdAndSellerSellerId(
            Long houseId,
            Long sellerId
    );
}