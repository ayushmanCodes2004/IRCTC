package com.irctc.userservice.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.userservice.DTO.OtpResultDTO;
import com.irctc.userservice.DTO.OtpSessionDataDTO;
import com.irctc.userservice.Exception.BadRequestException;
import com.irctc.userservice.Exception.TooManyRequestsException;
import com.irctc.userservice.Util.ExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int RATE_MAX = 5;
    private static final int OTP_TTL = 300;
    private static final int ATTEMPT_MAX = 5;
    private final ObjectMapper objectMapper;

    @Value("${otp.hmac.secret}")
    private String hmacSecret;

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpResultDTO generateAndStoreOtp(
            String firstName,
            String lastName,
            String email,
            String hashedPassword
    ) {
        String rateKey = "otp:rate:" + email;
        String countValue = redisTemplate.opsForValue().get(rateKey);
        int sentCount = countValue == null ? 0 : Integer.parseInt(countValue);

        if (sentCount >= RATE_MAX) {
            throw new TooManyRequestsException("Too many OTP requests. Try again later.");
        }

        String otp = generateOtp();
        String otpSessionId = UUID.randomUUID().toString();
        String hashedOtp = hmacFor(email, otp);
        String sessionKey = "otp:session:" + otpSessionId;

        OtpSessionDataDTO sessionData = new OtpSessionDataDTO(
                hashedOtp,
                firstName,
                lastName,
                email,
                hashedPassword
        );


        String sessionJson = ExceptionHandler.handleChecked(
                () -> objectMapper.writeValueAsString(sessionData),
                () -> new IllegalStateException("Failed to serialize OTP session data")
        );

        redisTemplate.opsForValue().set(sessionKey, sessionJson, OTP_TTL, TimeUnit.SECONDS);

        Long newCount = redisTemplate.opsForValue().increment(rateKey);
        if (newCount != null && newCount == 1) {
            redisTemplate.expire(rateKey, 1, TimeUnit.HOURS);
        }

        return new OtpResultDTO(otp, otpSessionId);
    }

    public OtpSessionDataDTO verifyOtp(String otpSessionId, String otp) {
        String sessionKey = "otp:session:" + otpSessionId;
        String sessionData = redisTemplate.opsForValue().get(sessionKey);

        if (sessionData == null) {
            throw new BadRequestException("Invalid or expired OTP session ID");
        }


        OtpSessionDataDTO session = ExceptionHandler.handleChecked(
                () -> objectMapper.readValue(sessionData, OtpSessionDataDTO.class),
                () -> new IllegalStateException("Invalid OTP session data")
        );

        String storedOtp = session.getHashedOtp();
        String email = session.getEmail();
        String attemptsKey = "otp:attempts:" + email;
        String attemptsValue = redisTemplate.opsForValue().get(attemptsKey);
        int attemptsCount = attemptsValue == null ? 0 : Integer.parseInt(attemptsValue);

        if (attemptsCount >= ATTEMPT_MAX) {
            throw new TooManyRequestsException("Too many attempts to verify OTP");
        }

        String hashedOtp = hmacFor(email, otp);
        boolean otpMatches = MessageDigest.isEqual(
                HexFormat.of().parseHex(hashedOtp),
                HexFormat.of().parseHex(storedOtp)
        );

        if (otpMatches) {
            redisTemplate.delete(sessionKey);
            redisTemplate.delete(attemptsKey);
            redisTemplate.delete("otp:rate:" + email);
            return session;
        }

        redisTemplate.opsForValue().increment(attemptsKey);
        redisTemplate.expire(attemptsKey, OTP_TTL, TimeUnit.SECONDS);

        throw new BadRequestException("Invalid OTP");
    }

    private String generateOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    private String hmacFor(String email, String otp) {
        return ExceptionHandler.handleChecked(() -> {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    hmacSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] hash = mac.doFinal((email + ":" + otp).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        }, () -> new IllegalStateException("Failed to generate OTP HMAC"));
    }
}
