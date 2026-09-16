package com.codelearn.houseselling.controller;

import com.codelearn.houseselling.dto.CustomerRequest;
import com.codelearn.houseselling.dto.CustomerResponse;
import com.codelearn.houseselling.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(
            CustomerService customerService) {

        this.customerService =
                customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse>
    createCustomer(
            @Valid @RequestBody
            CustomerRequest request) {

        CustomerResponse response =
                customerService
                        .createCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>>
    getAllCustomers() {

        return ResponseEntity.ok(
                customerService
                        .getAllCustomers()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse>
    getCustomerById(
            @PathVariable Long id) {

        CustomerResponse response =
                customerService
                        .getCustomerById(id);

        if (response == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse>
    updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody
            CustomerRequest request) {

        CustomerResponse response =
                customerService
                        .updateCustomer(
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
    deleteCustomer(
            @PathVariable Long id) {

        boolean deleted =
                customerService
                        .deleteCustomer(id);

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