package com.irctc.userservice.Service;

import com.irctc.userservice.DTO.*;
import com.irctc.userservice.Entity.User;
import com.irctc.userservice.Exception.InvalidTokenException;
import com.irctc.userservice.Exception.UnauthorizedException;
import com.irctc.userservice.Repository.UserRepository;
import com.irctc.userservice.Security.DeviceFingerprintService;
import com.irctc.userservice.Security.JwtUtil;
import com.irctc.userservice.Security.TokenSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String TOKEN_TYPE = "Bearer";

    private record Tokens(String accessToken, String refreshToken) {}

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenSessionService tokenSessionService;
    private final DeviceFingerprintService deviceFingerprintService;
    private final boolean enforceDeviceMatch;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       OtpService otpService,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       TokenSessionService tokenSessionService,
                       DeviceFingerprintService deviceFingerprintService,
                       @Value("${security.jwt.enforce-device-match}") boolean enforceDeviceMatch) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenSessionService = tokenSessionService;
        this.deviceFingerprintService = deviceFingerprintService;
        this.enforceDeviceMatch = enforceDeviceMatch;
    }


    public LoginResponseDTO login(LoginRequestDTO request, HttpServletRequest http) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        String deviceId = deviceFingerprintService.fingerprint(http);
        Tokens tokens = startSession(user.getId().toString(), deviceId);
        return loginResponse(tokens, "Logged in successfully", LoggedInUserDTO.from(user));
    }


    public OtpResultDTO sendOtp(RegisterRequestDTO request) {
        return otpService.generateAndStoreOtp(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
    }

    public User verifyOtp(String otpSessionId, String otp) {
        OtpSessionDataDTO otpSessionDataDTO = otpService.verifyOtp(otpSessionId, otp);
        if (otpSessionDataDTO == null) {
            throw new UnauthorizedException("Invalid OTP or OTP has expired.");
        }

        User user = new User();
        user.setFirstName(otpSessionDataDTO.getFirstName());
        user.setLastName(otpSessionDataDTO.getLastName());
        user.setEmail(otpSessionDataDTO.getEmail());
        user.setPassword(otpSessionDataDTO.getHashedPassword());
        user.setEmailVerified(true);

        return userRepository.save(user);
    }


    public TokenResponseDTO rotateRefreshToken(String refreshToken, HttpServletRequest http) {

        Jwt refreshJwt = jwtUtil.parseRefreshToken(refreshToken);
        String userId = jwtUtil.extractUserId(refreshJwt);
        String oldJti = jwtUtil.extractJti(refreshJwt);
        String deviceId = jwtUtil.extractDeviceId(refreshJwt);


        if (enforceDeviceMatch) {
            String currentDeviceId = deviceFingerprintService.fingerprint(http);
            boolean same = MessageDigest.isEqual(
                    deviceId.getBytes(StandardCharsets.UTF_8),
                    currentDeviceId.getBytes(StandardCharsets.UTF_8));
            if (!same) {
                log.warn("Refresh rejected for user {}: device mismatch", userId);
                throw new InvalidTokenException("Invalid device or session");
            }
        }


        if (!userRepository.existsById(java.util.UUID.fromString(userId))) {
            tokenSessionService.revokeSession(userId, deviceId);
            throw new InvalidTokenException("Invalid refresh token");
        }


        String newJti = jwtUtil.newJti();
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, deviceId, newJti);
        String newAccessToken = jwtUtil.generateAccessToken(userId);

        boolean rotated = tokenSessionService.rotateRefreshToken(
                userId, deviceId, oldJti, newJti, jwtUtil.getRefreshTtl());
        if (!rotated) {

            log.warn("Refresh rejected for user {}: token is stale, reused or session ended", userId);
            throw new InvalidTokenException("Refresh token is no longer valid");
        }

        return new TokenResponseDTO(
                true,
                "Token refreshed successfully",
                newAccessToken,
                newRefreshToken,
                TOKEN_TYPE,
                jwtUtil.getAccessTtl().toSeconds()
        );
    }

    public Optional<String> findEmailByUserId(String userId) {
        return userRepository.findById(java.util.UUID.fromString(userId))
                .map(User::getEmail);
    }

    private Tokens startSession(String userId, String deviceId) {
        String jti = jwtUtil.newJti();
        String refreshToken = jwtUtil.generateRefreshToken(userId, deviceId, jti);
        String accessToken = jwtUtil.generateAccessToken(userId);

        tokenSessionService.saveRefreshSession(userId, deviceId, jti, jwtUtil.getRefreshTtl());
        return new Tokens(accessToken, refreshToken);
    }

    private LoginResponseDTO loginResponse(Tokens tokens, String message, LoggedInUserDTO loggedInUserDTO) {
        return new LoginResponseDTO(
                true,
                message,
                tokens.accessToken(),
                tokens.refreshToken(),
                TOKEN_TYPE,
                jwtUtil.getAccessTtl().toSeconds(),
                loggedInUserDTO
        );
    }
}
