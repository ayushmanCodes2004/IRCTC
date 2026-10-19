package com.irctc.userservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenResponseDTO {
    private final boolean success;
    private final String message;
    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final long expiresIn;
}
