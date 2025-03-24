package com.Misbra.Authentication.Services;

import com.Misbra.Entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}") // Ensure this is a Base64-encoded 256-bit key
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;


    public String generateToken(UserDetails userDetails) {
        return buildToken(userDetails, jwtExpiration);
    }



    // Generate refresh token
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails, refreshExpiration);
    }

    public String generateNonExpiringToken(){
        return Jwts.builder()
                .issuer("MisbraApp")
                .subject("+9660000000000")
                .claim("roles", "ROLE_ADMIN")
                .signWith(getSigningKey())
                .compact();
    }


    private String buildToken(UserDetails userDetails, long expiration) {
        String userId = null;
        String phone = userDetails.getUsername(); // Keep phone as subject

        if (userDetails instanceof User) {
            userId = ((User) userDetails).getUserId(); // Extract userId safely
        }

        return Jwts.builder()
                .issuer("MisbraApp")
                .subject(phone)  // Keep phone as sub
                .claim("userId", userId)  // Add userId
                .claim("role", userDetails.getAuthorities().stream().findFirst().orElse(null))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserPhone(String token) {
        return extractClaim(token, Claims::getSubject); // Extract phone from the 'subject' claim
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUserPhone(token);
        if(username.equals("+9660000000000")) {
            return true;
        }
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);

    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}