package com.example.fas.security;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    // Key Secret Application
    @Value("${app.jwt.key.secret.application}")
    private String secretKey;

    private Key signingKey;

    private final Date currentDate = new Date(System.currentTimeMillis());

    @Value("${app.jwt.expiration.millis}")
    private long jwtExpiration;

    @PostConstruct
    public void jwtInit() {
        // Encode the secret key
        byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);

        // Convert to signing key
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(currentDate)
                .setExpiration(new Date(currentDate.getTime() + jwtExpiration))
                .signWith(this.signingKey)
                .compact();
    }
}
