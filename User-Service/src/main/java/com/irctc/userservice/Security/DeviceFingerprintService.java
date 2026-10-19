package com.irctc.userservice.Security;

import com.irctc.userservice.Util.ExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;


@Service
public class DeviceFingerprintService {

    public String fingerprint(HttpServletRequest request) {
        String userAgent = valueOrEmpty(request.getHeader("User-Agent"));
        String accept = valueOrEmpty(request.getHeader("Accept"));
        String ip = valueOrEmpty(request.getRemoteAddr());

        String raw = userAgent + "|" + ip + "|" + accept;


        return ExceptionHandler.handleChecked(
                () -> {
                    byte[] hash = MessageDigest.getInstance("SHA-256")
                            .digest(raw.getBytes(StandardCharsets.UTF_8));
                    return HexFormat.of().formatHex(hash);
                },
                () -> new IllegalStateException("SHA-256 not available")
        );
    }

    private static String valueOrEmpty(String s) {
        return s == null ? "" : s;
    }
}
