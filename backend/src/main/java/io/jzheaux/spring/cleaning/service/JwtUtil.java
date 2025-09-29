package io.jzheaux.spring.cleaning.service;

import io.jzheaux.spring.cleaning.dto.UserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for creating and validating JSON Web Tokens (JWTs).
 * Encodes user email and ID into the token and supports robust validation
 * via signature and expiry checks.
 */
@Component
public class JwtUtil {

    private final String SECRET;
    private static final Duration DEFAULT_ACCESS_TTL = Duration.ofMinutes(15);

    /**
     * Initializes the utility with the JWT secret key.
     *
     * @param secret The JWT secret key (injected via application properties).
     */
    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.SECRET = secret;
        System.out.println("JWT SECRET LOADED: " + secret);
    }

    /**
     * Generates a signed JWT token for the given user with a specified TTL.
     *
     * @param user The user for whom to generate the token.
     * @param ttl  Token time-to-live (e.g., 15 minutes).
     * @return A signed JWT string.
     */
    public String generateToken(UserDTO user, Duration ttl) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.id());

        Date now = new Date();
        Date exp = new Date(now.getTime() + ttl.toMillis());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.email())          // sub = email
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generates a signed JWT token using a default TTL (15 minutes).
     *
     * @param user The user for whom to generate the token.
     * @return A signed JWT string.
     */
    public String generateToken(UserDTO user) {
        return generateToken(user, DEFAULT_ACCESS_TTL);
    }

    /**
     * Extracts the email (subject) from a JWT token.
     *
     * @param token The JWT string.
     * @return The user's email, or null if extraction fails.
     */
    public String extractEmail(String token) {
        try {
            return parse(token).getBody().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts all claims from a JWT.
     *
     * @param token The JWT string.
     * @return The {@link Claims} object containing all token data.
     */
    public Claims extractAllClaims(String token) {
        return parse(token).getBody();
    }

    /**
     * Validates a token by checking its signature, expiry, and subject (email) against the provided user details.
     *
     * @param token The JWT string.
     * @param ud    The user details to match.
     * @return True if the token is valid and belongs to the user.
     */
    public boolean isTokenValid(String token, UserDetails ud) {
        try {
            Claims c = extractAllClaims(token);
            return c.getSubject().equals(ud.getUsername());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates a token by checking signature and expiry only.
     * This method is unused but retained for convenience.
     *
     * @param token The JWT string.
     * @return True if the token is valid; false otherwise.
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Internal helper to parse a JWT string and return its JWS claims.
     *
     * @param token The JWT string.
     * @return Parsed {@link Jws} object with claims.
     */
    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token);
    }
}
