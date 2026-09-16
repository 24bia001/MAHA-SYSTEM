package com.codelearn.houseselling.controller;

import com.codelearn.houseselling.dto.BookingResponse;
import com.codelearn.houseselling.dto.CustomerResponse;
import com.codelearn.houseselling.dto.DocumentResponse;
import com.codelearn.houseselling.dto.HouseResponse;
import com.codelearn.houseselling.dto.ManagementRequest;
import com.codelearn.houseselling.dto.ManagementResponse;
import com.codelearn.houseselling.dto.PaymentResponse;
import com.codelearn.houseselling.dto.SaleResponse;
import com.codelearn.houseselling.dto.SellerResponse;
import com.codelearn.houseselling.dto.SellerRequest;
import com.codelearn.houseselling.dto.SellerUpdateRequest;

import com.codelearn.houseselling.service.ManagementService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/management")
public class ManagementController {

    private final ManagementService managementService;

    public ManagementController(
            ManagementService managementService) {

        this.managementService =
                managementService;
    }

    // =========================
    // MANAGEMENT CRUD
    // =========================

    @PostMapping
    public ResponseEntity<ManagementResponse>
    createManagement(
            @Valid @RequestBody
            ManagementRequest request) {

        ManagementResponse response =
                managementService
                        .createManagement(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ManagementResponse>>
    getAllManagement() {

        return ResponseEntity.ok(
                managementService
                        .getAllManagement()
        );
    }

    // =========================
    // ADMIN: CREATE SELLER
    // =========================

    @PostMapping("/sellers")
    public ResponseEntity<SellerResponse> createSeller(
            @Valid @RequestBody SellerRequest request) {

        SellerResponse response = managementService.createSeller(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================
    // ADMIN: SELLERS
    // =========================

    @GetMapping("/sellers")
    public ResponseEntity<List<SellerResponse>>
    getAllSellers() {

        return ResponseEntity.ok(
                managementService
                        .getAllSellers()
        );
    }

    @PutMapping("/sellers/{id}")
    public ResponseEntity<SellerResponse> updateSeller(
            @PathVariable Long id,
            @Valid @RequestBody SellerUpdateRequest request) {
        SellerResponse response = managementService.updateSeller(id, request);
        return response == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(response);
    }

    @DeleteMapping("/sellers/{id}")
    public ResponseEntity<Void> deleteSeller(@PathVariable Long id) {
        managementService.deleteSeller(id);
        return ResponseEntity.noContent().build();
    }

    // =========================
    // ADMIN: HOUSES
    // =========================

    @GetMapping("/houses")
    public ResponseEntity<List<HouseResponse>>
    getAllHouses() {

        return ResponseEntity.ok(
                managementService
                        .getAllHouses()
        );
    }

    // =========================
    // ADMIN: BOOKINGS
    // =========================

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>>
    getAllBookings() {

        return ResponseEntity.ok(
                managementService
                        .getAllBookings()
        );
    }

    // =========================
    // ADMIN: PAYMENTS
    // =========================

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponse>>
    getAllPayments() {

        return ResponseEntity.ok(
                managementService
                        .getAllPayments()
        );
    }

    // =========================
    // ADMIN: SALES
    // =========================

    @GetMapping("/sales")
    public ResponseEntity<List<SaleResponse>>
    getAllSales() {

        return ResponseEntity.ok(
                managementService
                        .getAllSales()
        );
    }

    // =========================
    // ADMIN: DOCUMENTS
    // =========================

    @GetMapping("/documents")
    public ResponseEntity<List<DocumentResponse>>
    getAllDocuments() {

        return ResponseEntity.ok(
                managementService
                        .getAllDocuments()
        );
    }

    // =========================
    // ADMIN: CUSTOMERS
    // =========================

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerResponse>>
    getAllCustomers() {

        return ResponseEntity.ok(
                managementService
                        .getAllCustomers()
        );
    }

    // =========================
    // MANAGEMENT BY ID
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<ManagementResponse>
    getManagementById(
            @PathVariable Long id) {

        ManagementResponse response =
                managementService
                        .getManagementById(id);

        if (response == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                response
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ManagementResponse>
    updateManagement(
            @PathVariable Long id,
            @Valid @RequestBody
            ManagementRequest request) {

        ManagementResponse response =
                managementService
                        .updateManagement(
                                id,
                                request
                        );

        if (response == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                response
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteManagement(
            @PathVariable Long id) {

        managementService
                .deleteManagement(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}