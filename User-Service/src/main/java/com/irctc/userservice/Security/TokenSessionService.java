package com.irctc.userservice.Security;

import com.irctc.userservice.Exception.SessionStoreUnavailableException;
import com.irctc.userservice.Util.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;


@Service
public class TokenSessionService {

    private static final Logger log = LoggerFactory.getLogger(TokenSessionService.class);
    private static final String KEY_PREFIX = "auth:refresh:";


    private static final DefaultRedisScript<Long> ROTATE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                redis.call('SET', KEYS[1], ARGV[2], 'PX', ARGV[3])
                return 1
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redis;

    public TokenSessionService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void saveRefreshSession(String userId, String deviceId, String jti, Duration ttl) {

        ExceptionHandler.handleChecked(
                () -> {
                    redis.opsForValue().set(key(userId, deviceId), jti, ttl);
                    return null;
                },
                () -> unavailable("save")
        );
    }


    public boolean rotateRefreshToken(String userId, String deviceId, String oldJti, String newJti, Duration ttl) {

        return ExceptionHandler.handleChecked(
                () -> {
                    Long result = redis.execute(ROTATE_SCRIPT,
                            List.of(key(userId, deviceId)),
                            oldJti, newJti, String.valueOf(ttl.toMillis()));
                    return Long.valueOf(1L).equals(result);
                },
                () -> unavailable("rotate")
        );
    }

    public void revokeSession(String userId, String deviceId) {

        ExceptionHandler.handleChecked(
                () -> {
                    redis.delete(key(userId, deviceId));
                    return null;
                },
                () -> unavailable("revoke")
        );
    }

    private static String key(String userId, String deviceId) {
        return KEY_PREFIX + userId + ":" + deviceId;
    }

    private static SessionStoreUnavailableException unavailable(String operation) {

        log.error("Redis session store failed during '{}'", operation);
        return new SessionStoreUnavailableException("Session store unavailable");
    }
}
