package com.codelearn.houseselling.service;

import com.codelearn.houseselling.dto.LoginRequest;
import com.codelearn.houseselling.dto.ManagementLoginResponse;
import com.codelearn.houseselling.entity.Management;
import com.codelearn.houseselling.repository.ManagementRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ManagementAuthService {

    private static final int MAX_FAILED_ATTEMPTS = 7;
    private static final long LOCK_DURATION_MINUTES = 6;

    private final ManagementRepository managementRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public ManagementAuthService(
            ManagementRepository managementRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.managementRepository =
                managementRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.jwtService =
                jwtService;
    }

    public ManagementLoginResponse login(
            LoginRequest request) {

        Management management =
                managementRepository
                        .findByEmail(
                                request.getEmail()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid email or password"
                                )
                        );

        checkAccountLock(management);

        if (management.getPassword() == null
                || management.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Management account does not have a password"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                management.getPassword())) {

            registerFailedAttempt(management);
        }

        resetFailedAttempts(management);

        String role =
                management
                        .getRole()
                        .toUpperCase();

        String token =
                jwtService.generateToken(
                        management.getEmail(),
                        role
                );

        return new ManagementLoginResponse(
                management.getManagementId(),
                management.getName(),
                management.getEmail(),
                role,
                token,
                "Bearer",
                "Management login successful"
        );
    }

    private void checkAccountLock(
            Management management) {

        LocalDateTime lockedUntil =
                management.getAccountLockedUntil();

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

        management.setFailedLoginAttempts(0);
        management.setAccountLockedUntil(null);

        managementRepository.save(management);
    }

    private void registerFailedAttempt(
            Management management) {

        int currentAttempts =
                management.getFailedLoginAttempts() == null
                        ? 0
                        : management.getFailedLoginAttempts();

        int newAttempts =
                currentAttempts + 1;

        management.setFailedLoginAttempts(
                newAttempts
        );

        if (newAttempts >= MAX_FAILED_ATTEMPTS) {

            management.setAccountLockedUntil(
                    LocalDateTime.now()
                            .plusMinutes(
                                    LOCK_DURATION_MINUTES
                            )
            );

            managementRepository.save(
                    management
            );

            throw new IllegalArgumentException(
                    "Too many failed login attempts. Account locked for 6 minutes."
            );
        }

        managementRepository.save(
                management
        );

        throw new IllegalArgumentException(
                "Invalid email or password"
        );
    }

    private void resetFailedAttempts(
            Management management) {

        Integer attempts =
                management.getFailedLoginAttempts();

        if ((attempts != null && attempts > 0)
                || management.getAccountLockedUntil() != null) {

            management.setFailedLoginAttempts(0);
            management.setAccountLockedUntil(null);

            managementRepository.save(
                    management
            );
        }
    }
}