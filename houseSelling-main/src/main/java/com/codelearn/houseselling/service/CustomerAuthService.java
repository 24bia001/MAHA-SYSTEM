package com.codelearn.houseselling.service;

import com.codelearn.houseselling.dto.CustomerLoginResponse;
import com.codelearn.houseselling.dto.CustomerRequest;
import com.codelearn.houseselling.dto.CustomerResponse;
import com.codelearn.houseselling.dto.LoginRequest;
import com.codelearn.houseselling.entity.Customer;
import com.codelearn.houseselling.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomerAuthService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MINUTES = 15;

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public CustomerAuthService(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // =========================
    // CUSTOMER REGISTRATION
    // =========================

    public CustomerResponse registerCustomer(
            CustomerRequest request) {

        if (customerRepository.existsByEmail(
                request.getEmail())) {

            throw new IllegalArgumentException(
                    "Customer email already exists: "
                            + request.getEmail()
            );
        }

        Customer customer =
                new Customer();

        customer.setName(
                request.getName()
        );

        customer.setEmail(
                request.getEmail()
        );

        customer.setPhone(
                request.getPhone()
        );

        customer.setAddress(
                request.getAddress()
        );

        customer.setNida(request.getNida());

        customer.setImage(request.getImage());

        customer.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        customer.setFailedLoginAttempts(0);
        customer.setAccountLockedUntil(null);

        Customer savedCustomer =
                customerRepository.save(
                        customer
                );

        return convertToResponse(
                savedCustomer
        );
    }

    // =========================
    // CUSTOMER LOGIN
    // =========================

    public CustomerLoginResponse loginCustomer(
            LoginRequest request) {

        Customer customer =
                customerRepository
                        .findByEmail(
                                request.getEmail()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid email or password"
                                )
                        );

        checkAccountLock(customer);

        if (customer.getPassword() == null
                || customer.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Customer account does not have a password"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                customer.getPassword())) {

            registerFailedAttempt(customer);
        }

        resetFailedAttempts(customer);

        String token =
                jwtService.generateToken(
                        customer.getEmail(),
                        "CUSTOMER"
                );

        CustomerLoginResponse response = new CustomerLoginResponse(
                customer.getCustomerId(),
                customer.getName(),
                customer.getEmail(),
                "CUSTOMER",
                token,
                "Bearer",
                "Customer login successful"
        );
        response.setImage(customer.getImage());
        return response;
    }

    private void checkAccountLock(
            Customer customer) {

        LocalDateTime lockedUntil =
                customer.getAccountLockedUntil();

        if (lockedUntil == null) {
            return;
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (lockedUntil.isAfter(now)) {

            throw new IllegalArgumentException(
                    "Account is temporarily locked. Please try again later."
            );
        }

        customer.setFailedLoginAttempts(0);
        customer.setAccountLockedUntil(null);

        customerRepository.save(customer);
    }

    private void registerFailedAttempt(
            Customer customer) {

        int currentAttempts =
                customer.getFailedLoginAttempts() == null
                        ? 0
                        : customer.getFailedLoginAttempts();

        int newAttempts =
                currentAttempts + 1;

        customer.setFailedLoginAttempts(
                newAttempts
        );

        if (newAttempts >= MAX_FAILED_ATTEMPTS) {

            customer.setAccountLockedUntil(
                    LocalDateTime.now()
                            .plusMinutes(
                                    LOCK_DURATION_MINUTES
                            )
            );

            customerRepository.save(customer);

            throw new IllegalArgumentException(
                    "Too many failed login attempts. Account locked for 15 minutes."
            );
        }

        customerRepository.save(customer);

        throw new IllegalArgumentException(
                "Invalid email or password"
        );
    }

    private void resetFailedAttempts(
            Customer customer) {

        Integer attempts =
                customer.getFailedLoginAttempts();

        if ((attempts != null && attempts > 0)
                || customer.getAccountLockedUntil() != null) {

            customer.setFailedLoginAttempts(0);
            customer.setAccountLockedUntil(null);

            customerRepository.save(customer);
        }
    }

    private CustomerResponse convertToResponse(
            Customer customer) {

        CustomerResponse response =
                new CustomerResponse();

        response.setCustomerId(
                customer.getCustomerId()
        );

        response.setName(
                customer.getName()
        );

        response.setEmail(
                customer.getEmail()
        );

        response.setPhone(
                customer.getPhone()
        );

        response.setAddress(
                customer.getAddress()
        );

        response.setNida(customer.getNida());
        response.setImage(customer.getImage());

        return response;
    }
}