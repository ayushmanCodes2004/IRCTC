package com.irctc.userservice.DTO;

import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;

public record ExceptionResponseDTO(
        String path,
        HttpStatus status,
        String message,
        LocalDateTime timestamp
) {}
