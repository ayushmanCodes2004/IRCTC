package com.irctc.userservice.controller;

import com.irctc.userservice.dto.OtpResponse;
import com.irctc.userservice.dto.OtpResult;
import com.irctc.userservice.dto.RegisterRequest;
import com.irctc.userservice.dto.RegistrationResponse;
import com.irctc.userservice.entity.User;
import com.irctc.userservice.error.BadRequestError;
import com.irctc.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/otp")
    public ResponseEntity<OtpResponse> sendOtp(
            @Valid @RequestBody RegisterRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(
                    new OtpResponse(
                            false,
                            "Passwords do not match",
                            null
                    )
            );

        }

        OtpResult otpResult = authService.sendOtp(request);

        OtpResponse response = new OtpResponse(
                true,
                "OTP sent successfully",
                otpResult
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<RegistrationResponse> verifyOTP(@RequestBody OtpResult otpResult) {

        String otpSessionId = otpResult.otpSessionId();
        String otp = otpResult.otp();

        if(otpSessionId == null || otp == null) {
            throw new BadRequestError("OTP session ID and OTP must be provided");
        }
        User user = authService.verifyOtp(otpSessionId, otp);

        return ResponseEntity.ok(
                new RegistrationResponse(
                        true,
                        "User registered successfully",
                        user
                )

        );


    }
}