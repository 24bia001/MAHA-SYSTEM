package com.codelearn.houseselling.config;

import com.codelearn.houseselling.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http
                .cors(cors -> {})
                .csrf(csrf ->
                        csrf.disable()
                )

                .formLogin(form ->
                        form.disable()
                )

                .httpBasic(basic ->
                        basic.disable()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth ->
                        auth

                                // =========================
                                // PUBLIC LOGIN
                                // =========================

                                .requestMatchers(
                                        "/api/auth/**"
                                )
                                .permitAll()

                                // =========================
                                // ADMIN CREATES SELLERS
                                // =========================

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/sellers"
                                )
                                .hasRole("ADMIN")

                                // Public property catalogue for the landing page
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/houses",
                                        "/api/houses/{id}"
                                )
                                .permitAll()

                                // Allow browser CORS preflight
                                .requestMatchers(HttpMethod.OPTIONS, "/**")
                                .permitAll()

                                // =========================
                                // ADMIN ONLY
                                // =========================

                                .requestMatchers(
                                        "/api/management/**"
                                )
                                .hasRole("ADMIN")

                                // =========================
                                // CUSTOMER ONLY
                                // singular /customer
                                // =========================

                                .requestMatchers(
                                        "/api/customer/**"
                                )
                                .hasRole("CUSTOMER")

                                // =========================
                                // SELLER ONLY
                                // plural /customers
                                // =========================

                                .requestMatchers(
                                        "/api/houses/**",
                                        "/api/bookings/**",
                                        "/api/payments/**",
                                        "/api/sales/**",
                                        "/api/documents/**",
                                        "/api/customers/**",
                                        "/api/sellers/**"
                                )
                                .hasRole("SELLER")

                                .anyRequest()
                                .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}