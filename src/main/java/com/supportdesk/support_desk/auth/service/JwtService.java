package com.supportdesk.support_desk.auth.service;


import com.supportdesk.support_desk.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // @Value reads properties from application.properties
    // application.jwt.secret-key → the hex string we set
    @Value("${application.jwt.secret-key}")
    private String secretKey;

    @Value("${application.jwt.expiration-ms}")
    private long expirationMs;

    //token ... generation
    public String generateToken(User user) {
        // Extra claims we embed in the JWT payload
        // These are read on every request so we don't need extra DB queries
        // for basic user info (we still check DB for isActive)
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("firstName", user.getFirstName());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getEmail())            // "sub" claim = email
                .issuedAt(new Date())                // "iat" claim = now
                .expiration(new Date(System.currentTimeMillis() + expirationMs))  // "exp"
                .signWith(getSigningKey())            // signs with HMAC-SHA256
                .compact();                           // builds the final string
    }


    // TOKEN VALIDATION
    // Called by JwtAuthFilter on every request that has a token

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);

        // Token is valid if:
        // 1. The email in the token matches the loaded user's email
        // 2. The token is not expired
        // Note: isEnabled() (isActive check) is handled by Spring Security
        //       automatically when it processes the UserDetails object
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }


    // CLAIM EXTRACTION — read data from the JWT payload

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // Generic extractor — accepts any function that maps Claims → T
    // This pattern avoids writing a separate method for every claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ----------------------------------------------------------------
    // PRIVATE HELPERS
    // ----------------------------------------------------------------

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // uses secret to verify signature
                .build()
                .parseSignedClaims(token)      // throws if signature invalid or expired
                .getPayload();
    }

    private SecretKey getSigningKey() {
        // Decode our hex secret key string into bytes
        // then create an HMAC-SHA key from those bytes
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}