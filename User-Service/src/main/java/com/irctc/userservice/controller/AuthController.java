package com.irctc.userservice.Controller;

import com.irctc.userservice.DTO.*;
import com.irctc.userservice.Entity.User;
import com.irctc.userservice.Exception.BadRequestException;
import com.irctc.userservice.Service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/otp")
    public ResponseEntity<OtpResponseDTO> sendOtp(@Valid @RequestBody RegisterRequestDTO request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(
                    new OtpResponseDTO(
                            false,
                            "Passwords do not match",
                            null
                    )
            );
        }

        OtpResultDTO otpResultDTO = authService.sendOtp(request);
        return ResponseEntity.ok(new OtpResponseDTO(
                true,
                "OTP sent successfully",
                otpResultDTO
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<RegistrationResponseDTO> verifyOTP(@RequestBody OtpResultDTO otpResultDTO) {
        String otpSessionId = otpResultDTO.otpSessionId();
        String otp = otpResultDTO.otp();

        if (otpSessionId == null || otp == null) {
            throw new BadRequestException("OTP session ID and OTP must be provided");
        }

        User user = authService.verifyOtp(otpSessionId, otp);
        return ResponseEntity.ok(new RegistrationResponseDTO(
                true,
                "User registered successfully",
                user
        ));
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request,
                                                  HttpServletRequest http) {
        LoginResponseDTO loginResponseDTO = authService.login(request, http);
        return ResponseEntity.ok(loginResponseDTO);
    }


    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request,
                                                    HttpServletRequest http) {
        TokenResponseDTO tokenResponseDTO = authService.rotateRefreshToken(request.getRefreshToken(), http);
        return ResponseEntity.ok(tokenResponseDTO);
    }
}
