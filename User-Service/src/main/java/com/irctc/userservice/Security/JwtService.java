package com.irctc.userservice.Security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;


@Configuration
public class JwtService {

    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    public JwtService(@Value("${security.jwt.access-secret}") String accessSecret,
                      @Value("${security.jwt.refresh-secret}") String refreshSecret) {
        if (accessSecret.equals(refreshSecret)) {
            throw new IllegalStateException("security.jwt.access-secret and security.jwt.refresh-secret must be different");
        }
        this.accessKey = toKey(accessSecret, "security.jwt.access-secret");
        this.refreshKey = toKey(refreshSecret, "security.jwt.refresh-secret");
    }

    private static SecretKey toKey(String secret, String propertyName) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(propertyName + " must be at least " + MIN_SECRET_BYTES + " bytes long");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean(name = "accessJwtEncoder")
    public JwtEncoder accessJwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(accessKey));
    }

    @Bean(name = "refreshJwtEncoder")
    public JwtEncoder refreshJwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(refreshKey));
    }


    @Bean(name = "accessJwtDecoder")
    public JwtDecoder accessJwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(accessKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        OAuth2TokenValidator<Jwt> typeValidator = jwt ->
                JwtUtil.TYPE_ACCESS.equals(jwt.getClaimAsString(JwtUtil.CLAIM_TYPE))
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Wrong token type", null));

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(), typeValidator));
        return decoder;
    }


    @Bean(name = "refreshJwtDecoder")
    public JwtDecoder refreshJwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(refreshKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
