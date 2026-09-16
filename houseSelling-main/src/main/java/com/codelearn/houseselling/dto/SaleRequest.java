package com.codelearn.houseselling.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class SaleRequest {

    @NotNull(message = "Sale price is required")
    @Positive(message = "Sale price must be greater than zero")
    private Double salePrice;

    @NotNull(message = "Sale date is required")
    @PastOrPresent(message = "Sale date cannot be in the future")
    private LocalDate saleDate;

    @NotNull(message = "House ID is required")
    private Long houseId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    public Double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(
            Double salePrice) {

        this.salePrice = salePrice;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(
            LocalDate saleDate) {

        this.saleDate = saleDate;
    }

    public Long getHouseId() {
        return houseId;
    }

    public void setHouseId(
            Long houseId) {

        this.houseId = houseId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(
            Long customerId) {

        this.customerId = customerId;
    }
}