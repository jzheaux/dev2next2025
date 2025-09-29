package io.jzheaux.spring.cleaning.service;

import io.jzheaux.spring.cleaning.dto.RefreshTokenDTO;
import io.jzheaux.spring.cleaning.exceptions.RefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private static final Duration DEFAULT_TTL = Duration.ofDays(7);

    private Map<UUID, RefreshTokenDTO> tokens = new HashMap<>();

    /**
     * Creates a new refresh token for the given user, optionally with a longer TTL if "remember me" is selected.
     * Replaces any existing token for the user.
     *
     * @param userId     ID of the user to associate with the token.
     * @param rememberMe If true, extends the token TTL (e.g., 28 days).
     * @return A new random refresh token string.
     */
    public String create(UUID userId, boolean rememberMe) {
        Duration ttl = rememberMe ? DEFAULT_TTL.multipliedBy(4) : DEFAULT_TTL;
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(ttl.getSeconds());
        this.tokens.put(userId, new RefreshTokenDTO(userId, token, expiry));
        return token;
    }

    /**
     * Verifies the given refresh token and returns the associated user ID if valid.
     * Automatically deletes expired tokens.
     *
     * @param token The refresh token to verify.
     * @return The ID of the user associated with the token.
     * @throws RefreshTokenException if the token is invalid or expired.
     */
    public UUID verifyAndGetUserId(String token) {
        for (var refreshToken : this.tokens.values()) {
            if (!refreshToken.refreshToken().equals(token)) {
                continue;
            }
            if (refreshToken.expiry().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
                throw new RefreshTokenException("refresh token expired");
            }
            return refreshToken.userId();
        }
        throw new RefreshTokenException("invalid refresh token");
    }

    /**
     * Invalidates a refresh token by removing it from the database.
     *
     * @param token The token to invalidate.
     */
    public void invalidate(String token) {
        Map<UUID, RefreshTokenDTO> tokens = new HashMap<>();
        for (Map.Entry<UUID, RefreshTokenDTO> entry : this.tokens.entrySet()) {
            if (!entry.getValue().refreshToken().equals(token)) {
                tokens.put(entry.getKey(), entry.getValue());
            }
        }
        this.tokens = tokens;
    }

}
