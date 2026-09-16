package com.codelearn.houseselling.config;

import com.codelearn.houseselling.entity.Management;
import com.codelearn.houseselling.repository.ManagementRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminDataInitializer {

    @Bean
    CommandLineRunner createDefaultAdmin(
            ManagementRepository managementRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            String email = "admin@mahaehousing.co.tz";

            if (managementRepository.findByEmail(email).isEmpty()) {
                Management admin = new Management();
                admin.setName("MAHA E-HOUSING Administrator");
                admin.setEmail(email);
                admin.setPhone("0000000000");
                admin.setRole("ADMIN");
                admin.setPassword(passwordEncoder.encode("Admin@12345"));
                admin.setFailedLoginAttempts(0);
                admin.setAccountLockedUntil(null);
                managementRepository.save(admin);
                System.out.println("Default MAHA E-HOUSING admin created: " + email);
            }
        };
    }
}
