package com.tarkovcommunity.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final TypeReference<Map<String, Object>> CLAIMS_TYPE = new TypeReference<>() {
    };

    private final SysUserMapper userMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.auth.jwt.secret:tarkov-community-dev-secret-change-me}")
    private String tokenSecret = "tarkov-community-dev-secret-change-me";

    @Value("${app.auth.jwt.issuer:tarkov-community}")
    private String tokenIssuer = "tarkov-community";

    @Value("${app.auth.jwt.minutes:720}")
    private long tokenMinutes = 720L;

    public String createToken(SysUser user) {
        long now = Instant.now().getEpochSecond();
        long expiresAt = now + Math.max(5L, tokenMinutes) * 60L;

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", String.valueOf(user.getId()));
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        claims.put("iat", now);
        claims.put("exp", expiresAt);
        claims.put("iss", tokenIssuer);

        String unsignedToken = encodeJson(header) + "." + encodeJson(claims);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public Optional<SysUser> resolveUser(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }

        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3 || !StringUtils.hasText(parts[0]) || !StringUtils.hasText(parts[1])
                    || !StringUtils.hasText(parts[2])) {
                return Optional.empty();
            }

            String unsignedToken = parts[0] + "." + parts[1];
            if (!signatureMatches(sign(unsignedToken), parts[2])) {
                return Optional.empty();
            }

            Map<String, Object> header = decodeJson(parts[0]);
            if (!"HS256".equals(asString(header.get("alg"))) || !"JWT".equals(asString(header.get("typ")))) {
                return Optional.empty();
            }

            Map<String, Object> claims = decodeJson(parts[1]);
            if (!tokenIssuer.equals(asString(claims.get("iss")))) {
                return Optional.empty();
            }

            Long expiresAt = asLong(claims.get("exp"));
            if (expiresAt == null || expiresAt <= Instant.now().getEpochSecond()) {
                return Optional.empty();
            }

            Long userId = asLong(claims.get("sub"));
            String username = asString(claims.get("username"));
            if (userId == null || !StringUtils.hasText(username)) {
                return Optional.empty();
            }

            SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getId, userId)
                    .eq(SysUser::getUsername, username));

            if (user == null || !"NORMAL".equals(user.getStatus())) {
                return Optional.empty();
            }

            return Optional.of(user);
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return encode(objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to encode auth token", exception);
        }
    }

    private Map<String, Object> decodeJson(String value) {
        try {
            return objectMapper.readValue(decode(value), CLAIMS_TYPE);
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid auth token JSON", exception);
        }
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to sign auth token", exception);
        }
    }

    private static boolean signatureMatches(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof String text && StringUtils.hasText(text)) {
            return Long.valueOf(text);
        }

        return null;
    }
}
