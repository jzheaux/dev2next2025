package io.jzheaux.spring.cleaning.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RefreshTokenDTO(UUID userId, String refreshToken, LocalDateTime expiry) {
}
