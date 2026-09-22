package com.codelearn.houseselling.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {

        byte[] keyBytes =
                Decoders.BASE64.decode(jwtSecret);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(
            String email,
            String role) {

        Date now = new Date();

        return Jwts.builder()
                .subject(email)
                .claim(
                        "role",
                        role.toUpperCase()
                )
                .issuedAt(now)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(
            String token) {

        return extractClaims(token)
                .getSubject();
    }

    public String extractRole(
            String token) {

        return extractClaims(token)
                .get(
                        "role",
                        String.class
                );
    }

    public boolean isTokenValid(
            String token,
            String email) {

        try {

            Claims claims =
                    extractClaims(token);

            return email.equals(
                    claims.getSubject()
            );

        } catch (
                JwtException
                | IllegalArgumentException e) {

            return false;
        }
    }

    private Claims extractClaims(
            String token) {

        return Jwts.parser()
                .verifyWith(
                        getSigningKey()
                )
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
