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
                                        "/api/auth/**",
                                        "/api/health"
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

                                // Seller house management
                                .requestMatchers(HttpMethod.POST, "/api/houses").hasRole("SELLER")
                                .requestMatchers(HttpMethod.PUT, "/api/houses/{id}").hasRole("SELLER")
                                .requestMatchers(HttpMethod.DELETE, "/api/houses/{id}").hasRole("SELLER")

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
        // The API uses JWT Authorization headers rather than cookies.
        // This allows the local frontend and deployed frontend to call the API.
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}