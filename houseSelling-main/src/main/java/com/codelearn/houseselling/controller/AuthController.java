package com.codelearn.houseselling.controller;

import com.codelearn.houseselling.dto.CustomerLoginResponse;
import com.codelearn.houseselling.dto.CustomerRequest;
import com.codelearn.houseselling.dto.CustomerResponse;
import com.codelearn.houseselling.dto.LoginRequest;
import com.codelearn.houseselling.dto.LoginResponse;
import com.codelearn.houseselling.dto.ManagementLoginResponse;
import com.codelearn.houseselling.service.AuthService;
import com.codelearn.houseselling.service.CustomerAuthService;
import com.codelearn.houseselling.service.ManagementAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    private final ManagementAuthService
            managementAuthService;

    private final CustomerAuthService
            customerAuthService;

    public AuthController(
            AuthService authService,
            ManagementAuthService managementAuthService,
            CustomerAuthService customerAuthService) {

        this.authService = authService;

        this.managementAuthService =
                managementAuthService;

        this.customerAuthService =
                customerAuthService;
    }

    // =========================
    // SELLER LOGIN
    // =========================

    @PostMapping("/seller/login")
    public ResponseEntity<LoginResponse>
    loginSeller(
            @Valid @RequestBody
            LoginRequest request) {

        return ResponseEntity.ok(
                authService
                        .loginSeller(request)
        );
    }

    // =========================
    // MANAGEMENT LOGIN
    // =========================

    @PostMapping("/management/login")
    public ResponseEntity<ManagementLoginResponse>
    loginManagement(
            @Valid @RequestBody
            LoginRequest request) {

        return ResponseEntity.ok(
                managementAuthService
                        .login(request)
        );
    }

    // =========================
    // CUSTOMER REGISTER
    // =========================

    @PostMapping("/customer/register")
    public ResponseEntity<CustomerResponse>
    registerCustomer(
            @Valid @RequestBody
            CustomerRequest request) {

        CustomerResponse response =
                customerAuthService
                        .registerCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================
    // CUSTOMER LOGIN
    // =========================

    @PostMapping("/customer/login")
    public ResponseEntity<CustomerLoginResponse>
    loginCustomer(
            @Valid @RequestBody
            LoginRequest request) {

        return ResponseEntity.ok(
                customerAuthService
                        .loginCustomer(request)
        );
    }
}