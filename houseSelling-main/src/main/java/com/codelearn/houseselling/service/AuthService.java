package com.codelearn.houseselling.service;

import com.codelearn.houseselling.dto.LoginRequest;
import com.codelearn.houseselling.dto.LoginResponse;
import com.codelearn.houseselling.entity.Seller;
import com.codelearn.houseselling.repository.SellerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MINUTES = 15;

    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            SellerRepository sellerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.sellerRepository = sellerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse loginSeller(
            LoginRequest request) {

        Seller seller =
                sellerRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid email or password"
                                )
                        );

        // Check kama account bado locked
        checkAccountLock(seller);

        // Password wrong
        if (!passwordEncoder.matches(
                request.getPassword(),
                seller.getPassword())) {

            registerFailedAttempt(seller);
        }

        // Login successful
        resetFailedAttempts(seller);

        String token =
                jwtService.generateToken(
                        seller.getEmail(),
                        "SELLER"
                );

        LoginResponse response =
                new LoginResponse();

        response.setSellerId(
                seller.getSellerId()
        );

        response.setName(
                seller.getName()
        );

        response.setEmail(
                seller.getEmail()
        );

        response.setImage(
                seller.getImage()
        );

        response.setToken(
                token
        );

        response.setTokenType(
                "Bearer"
        );

        response.setMessage(
                "Seller login successful"
        );

        return response;
    }

    private void checkAccountLock(
            Seller seller) {

        LocalDateTime lockedUntil =
                seller.getAccountLockedUntil();

        if (lockedUntil == null) {
            return;
        }

        LocalDateTime now =
                LocalDateTime.now();

        // Account bado locked
        if (lockedUntil.isAfter(now)) {

            throw new IllegalArgumentException(
                    "Account is temporarily locked. Please try again later."
            );
        }

        // Lock ime-expire
        seller.setFailedLoginAttempts(0);
        seller.setAccountLockedUntil(null);

        sellerRepository.save(seller);
    }

    private void registerFailedAttempt(
            Seller seller) {

        int currentAttempts =
                seller.getFailedLoginAttempts() == null
                        ? 0
                        : seller.getFailedLoginAttempts();

        int newAttempts =
                currentAttempts + 1;

        seller.setFailedLoginAttempts(
                newAttempts
        );

        // Wrong password mara ya 3
        if (newAttempts >= MAX_FAILED_ATTEMPTS) {

            seller.setAccountLockedUntil(
                    LocalDateTime.now()
                            .plusMinutes(
                                    LOCK_DURATION_MINUTES
                            )
            );

            sellerRepository.save(seller);

            throw new IllegalArgumentException(
                    "Too many failed login attempts. Account locked for 15 minutes."
            );
        }

        sellerRepository.save(seller);

        throw new IllegalArgumentException(
                "Invalid email or password"
        );
    }

    private void resetFailedAttempts(
            Seller seller) {

        Integer attempts =
                seller.getFailedLoginAttempts();

        if ((attempts != null && attempts > 0)
                || seller.getAccountLockedUntil() != null) {

            seller.setFailedLoginAttempts(0);
            seller.setAccountLockedUntil(null);

            sellerRepository.save(seller);
        }
    }
}