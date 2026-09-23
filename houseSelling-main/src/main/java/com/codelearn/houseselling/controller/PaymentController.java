package com.codelearn.houseselling.controller;

import com.codelearn.houseselling.dto.PaymentRequest;
import com.codelearn.houseselling.dto.PaymentResponse;
import com.codelearn.houseselling.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService) {

        this.paymentService =
                paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse>
    createPayment(
            @Valid @RequestBody
            PaymentRequest request) {

        PaymentResponse response =
                paymentService.createPayment(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>>
    getAllPayments() {

        return ResponseEntity.ok(
                paymentService.getAllPayments()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse>
    getPaymentById(
            @PathVariable Long id) {

        PaymentResponse response =
                paymentService
                        .getPaymentById(id);

        if (response == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/receive")
    public ResponseEntity<PaymentResponse> receivePayment(
            @PathVariable Long id) {

        return ResponseEntity.ok(paymentService.receivePayment(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse>
    updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody
            PaymentRequest request) {

        PaymentResponse response =
                paymentService.updatePayment(
                        id,
                        request
                );

        if (response == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deletePayment(
            @PathVariable Long id) {

        boolean deleted =
                paymentService.deletePayment(id);

        if (!deleted) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity
                .noContent()
                .build();
    }
}