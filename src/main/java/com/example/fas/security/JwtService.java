package com.example.fas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";

    @Value("${app.jwt.key.secret.application}")
    private String secretKey;

    private Key signingKey;

    @Value("${app.jwt.expiration.millis}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh.expiration.millis}")
    private long refreshTokenExpiration;

    @Value("${app.jwt.allowedClockSkewSeconds:60}")
    private long allowedClockSkewSeconds;

    @PostConstruct
    public void jwtInit() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String username) {
        return buildToken(username, jwtExpiration, TOKEN_TYPE_ACCESS);
    }

    /**
     * Backwards-compatible alias for {@link #generateAccessToken(String)}.
     */
    @Deprecated
    public String generateToken(String username) {
        return generateAccessToken(username);
    }

    public String generateRefreshToken(String username) {
        return buildToken(username, refreshTokenExpiration, TOKEN_TYPE_REFRESH);
    }

    private String buildToken(String username, long lifespanMillis, String tokenType) {
        long nowMillis = System.currentTimeMillis();
        Date issuedAt = new Date(nowMillis);
        Date expiration = new Date(nowMillis + lifespanMillis);
        return Jwts.builder()
                .subject(username)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .signWith(this.signingKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(this.signingKey)
                .clockSkewSeconds(this.allowedClockSkewSeconds)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private boolean hasTokenType(String token, String expectedType) {
        Object typeClaim = parseClaims(token).get(TOKEN_TYPE_CLAIM);
        if (typeClaim == null) {
            return TOKEN_TYPE_ACCESS.equals(expectedType);
        }
        return expectedType.equalsIgnoreCase(String.valueOf(typeClaim));
    }

    public boolean isAccessToken(String token) {
        return hasTokenType(token, TOKEN_TYPE_ACCESS);
    }

    public boolean isRefreshToken(String token) {
        return hasTokenType(token, TOKEN_TYPE_REFRESH);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String usernameFromToken = extractUsername(token);
        return usernameFromToken.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && isAccessToken(token);
    }

    public boolean isRefreshTokenValid(String token, String username) {
        return username.equals(extractUsername(token))
                && !isTokenExpired(token)
                && isRefreshToken(token);
    }

    public long getAccessTokenTtl() {
        return jwtExpiration;
    }

    public long getRefreshTokenTtl() {
        return refreshTokenExpiration;
    }
}