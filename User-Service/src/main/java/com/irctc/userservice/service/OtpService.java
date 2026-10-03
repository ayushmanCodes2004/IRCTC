package com.irctc.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.userservice.dto.OtpResult;
import com.irctc.userservice.dto.OtpSessionData;
import com.irctc.userservice.error.BadRequestError;
import com.irctc.userservice.error.TooManyRequestsError;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
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

    @Value("${OTP_HMAC_SECRET}")
    private String hmacSecret;

    private final StringRedisTemplate redisTemplate;


    private final SecureRandom secureRandom = new SecureRandom();

    public OtpResult generateAndStoreOtp(
            String firstName,
            String lastName,
            String email,
            String hashedPassword
    ) {


        String rateKey = "otp:rate:" + email;

        String countValue = redisTemplate.opsForValue().get(rateKey);

        int sentCount = countValue == null
                ? 0
                : Integer.parseInt(countValue);

        if (sentCount >= RATE_MAX) {
            throw new RuntimeException(
                    "Too many OTP requests. Try again later."
            );
        }


        String otp = generateOtp();

        String otpSessionId = UUID.randomUUID().toString();

        String hashedOtp = hmacFor(email, otp);

        String sessionKey = "otp:session:" + otpSessionId;

        OtpSessionData sessionData = new OtpSessionData(
                hashedOtp,
                firstName,
                lastName,
                email,
                hashedPassword
        );


        try {
            String sessionJson = objectMapper.writeValueAsString(sessionData);

            redisTemplate.opsForValue().set(
                    sessionKey,
                    sessionJson,
                    OTP_TTL,
                    TimeUnit.SECONDS
            );



            Long newCount = redisTemplate
                    .opsForValue()
                    .increment(rateKey);


            if (newCount != null && newCount == 1) {
                redisTemplate.expire(
                        rateKey,
                        1,
                        TimeUnit.HOURS
                );
            }
            return new OtpResult(otp, otpSessionId);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public OtpSessionData verifyOtp(String otpSessionId, String otp) {

        String sessionKey = "otp:session:" + otpSessionId;

        String sessionData = redisTemplate
                .opsForValue()
                .get(sessionKey);


        if (sessionData == null) {
            throw new BadRequestError(
                    "Invalid or expired OTP session ID"
            );
        }

        try {

            OtpSessionData session = objectMapper.readValue(
                    sessionData,
                    OtpSessionData.class
            );

            String storedOtp = session.getHashedOtp();
            String email = session.getEmail();

            String attemptsKey = "otp:attempts:" + email;

            String attemptsValue = redisTemplate
                    .opsForValue()
                    .get(attemptsKey);

            int attemptsCount = attemptsValue == null
                    ? 0
                    : Integer.parseInt(attemptsValue);

            if (attemptsCount >= ATTEMPT_MAX) {
                throw new TooManyRequestsError(
                        "Too many attempts to verify OTP"
                );
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


            redisTemplate.expire(
                    attemptsKey,
                    OTP_TTL,
                    TimeUnit.SECONDS
            );

            throw new BadRequestError("Invalid OTP");

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "Invalid OTP session data",
                    e
            );
        }
    }

    private String generateOtp() {

        int otp = 100000 + secureRandom.nextInt(900000);

        return String.valueOf(otp);
    }

    private String hmacFor(String email, String otp) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(
                    hmacSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            mac.init(secretKey);

            byte[] hash = mac.doFinal(
                    (email + ":" + otp).getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Failed to generate OTP HMAC",
                    e
            );
        }
    }
}