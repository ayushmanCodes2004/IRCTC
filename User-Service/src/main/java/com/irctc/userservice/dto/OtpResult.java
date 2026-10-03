package com.irctc.userservice.dto;

public record OtpResult(
        String otp,
        String otpSessionId
) {
}
