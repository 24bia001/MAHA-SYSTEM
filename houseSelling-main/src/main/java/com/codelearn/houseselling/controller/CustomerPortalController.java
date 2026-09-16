package com.codelearn.houseselling.controller;

import com.codelearn.houseselling.dto.BookingResponse;
import com.codelearn.houseselling.dto.CustomerBookingRequest;
import com.codelearn.houseselling.dto.CustomerRequest;
import com.codelearn.houseselling.dto.CustomerResponse;
import com.codelearn.houseselling.dto.DocumentResponse;
import com.codelearn.houseselling.dto.HouseResponse;
import com.codelearn.houseselling.dto.PaymentResponse;
import com.codelearn.houseselling.dto.PaymentRequest;
import com.codelearn.houseselling.dto.SaleResponse;
import com.codelearn.houseselling.service.CustomerPortalService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerPortalController {

    private final CustomerPortalService
            customerPortalService;

    public CustomerPortalController(
            CustomerPortalService customerPortalService) {

        this.customerPortalService =
                customerPortalService;
    }

    // =========================
    // PROFILE
    // =========================

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse>
    getMyProfile() {

        return ResponseEntity.ok(
                customerPortalService
                        .getMyProfile()
        );
    }

    @PutMapping("/me")
    public ResponseEntity<CustomerResponse>
    updateMyProfile(
            @Valid @RequestBody
            CustomerRequest request) {

        return ResponseEntity.ok(
                customerPortalService
                        .updateMyProfile(request)
        );
    }

    // =========================
    // AVAILABLE HOUSES
    // =========================

    @GetMapping("/houses")
    public ResponseEntity<List<HouseResponse>>
    getAvailableHouses() {

        return ResponseEntity.ok(
                customerPortalService
                        .getAvailableHouses()
        );
    }

    @GetMapping("/houses/{id}")
    public ResponseEntity<HouseResponse>
    getAvailableHouseById(
            @PathVariable Long id) {

        HouseResponse response =
                customerPortalService
                        .getAvailableHouseById(id);

        if (response == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                response
        );
    }

    // =========================
    // BOOKINGS
    // =========================

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse>
    createMyBooking(
            @Valid @RequestBody
            CustomerBookingRequest request) {

        BookingResponse response =
                customerPortalService
                        .createMyBooking(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>>
    getMyBookings() {

        return ResponseEntity.ok(
                customerPortalService
                        .getMyBookings()
        );
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse>
    getMyBookingById(
            @PathVariable Long id) {

        BookingResponse response =
                customerPortalService
                        .getMyBookingById(id);

        if (response == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                response
        );
    }

    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<BookingResponse>
    cancelMyBooking(
            @PathVariable Long id) {

        BookingResponse response =
                customerPortalService
                        .cancelMyBooking(id);

        if (response == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                response
        );
    }

    // =========================
    // PAYMENTS
    // =========================

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> createMyPayment(
            @Valid @RequestBody PaymentRequest request) {

        PaymentResponse response =
                customerPortalService.createMyPayment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponse>>
    getMyPayments() {

        return ResponseEntity.ok(
                customerPortalService
                        .getMyPayments()
        );
    }

    // =========================
    // SALES
    // =========================

    @GetMapping("/sales")
    public ResponseEntity<List<SaleResponse>>
    getMySales() {

        return ResponseEntity.ok(
                customerPortalService
                        .getMySales()
        );
    }

    // =========================
    // DOCUMENTS
    // =========================

    @GetMapping("/documents")
    public ResponseEntity<List<DocumentResponse>>
    getMyDocuments() {

        return ResponseEntity.ok(
                customerPortalService
                        .getMyDocuments()
        );
    }
}