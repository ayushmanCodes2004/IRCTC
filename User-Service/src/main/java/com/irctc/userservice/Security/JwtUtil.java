package com.irctc.userservice.Security;

import com.irctc.userservice.Exception.InvalidTokenException;
import com.irctc.userservice.Util.ExceptionHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;


@Service
public class JwtUtil {

    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_DEVICE_ID = "deviceId";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";


    private static final JwsHeader HS256_HEADER = JwsHeader.with(MacAlgorithm.HS256).build();

    private final JwtEncoder accessJwtEncoder;
    private final JwtEncoder refreshJwtEncoder;
    private final JwtDecoder refreshJwtDecoder;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtUtil(@Qualifier("accessJwtEncoder") JwtEncoder accessJwtEncoder,
                   @Qualifier("refreshJwtEncoder") JwtEncoder refreshJwtEncoder,
                   @Qualifier("refreshJwtDecoder") JwtDecoder refreshJwtDecoder,
                   @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
                   @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds) {
        this.accessJwtEncoder = accessJwtEncoder;
        this.refreshJwtEncoder = refreshJwtEncoder;
        this.refreshJwtDecoder = refreshJwtDecoder;
        this.accessTtl = Duration.ofSeconds(accessTtlSeconds);
        this.refreshTtl = Duration.ofSeconds(refreshTtlSeconds);
    }

    public String generateAccessToken(String userId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId)
                .issuedAt(now)
                .expiresAt(now.plus(accessTtl))
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .build();
        return accessJwtEncoder.encode(JwtEncoderParameters.from(HS256_HEADER, claims)).getTokenValue();
    }

    public String generateRefreshToken(String userId, String deviceId, String jti) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId)
                .id(jti)
                .issuedAt(now)
                .expiresAt(now.plus(refreshTtl))
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .claim(CLAIM_DEVICE_ID, deviceId)
                .build();
        return refreshJwtEncoder.encode(JwtEncoderParameters.from(HS256_HEADER, claims)).getTokenValue();
    }

    public String newJti() {
        return UUID.randomUUID().toString();
    }


    public Jwt parseRefreshToken(String token) {
        Jwt jwt = ExceptionHandler.handleChecked(
                () -> refreshJwtDecoder.decode(token),
                () -> new InvalidTokenException("Invalid or expired refresh token")
        );

        if (!TYPE_REFRESH.equals(jwt.getClaimAsString(CLAIM_TYPE))) {
            throw new InvalidTokenException("Wrong token type");
        }
        if (isBlank(extractUserId(jwt)) || isBlank(extractJti(jwt)) || isBlank(extractDeviceId(jwt))) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        return jwt;
    }

    public String extractUserId(Jwt jwt) {
        return jwt.getSubject();
    }

    public String extractJti(Jwt jwt) {
        return jwt.getId();
    }

    public String extractDeviceId(Jwt jwt) {
        return jwt.getClaimAsString(CLAIM_DEVICE_ID);
    }

    public Duration getAccessTtl() {
        return accessTtl;
    }

    public Duration getRefreshTtl() {
        return refreshTtl;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
