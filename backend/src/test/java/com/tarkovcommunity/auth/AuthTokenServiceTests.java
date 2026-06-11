package com.tarkovcommunity.auth;

import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AuthTokenServiceTests {

    private static final String TEST_SECRET = "test-secret-for-jwt-signing";

    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final AuthTokenService authTokenService = new AuthTokenService(userMapper);

    AuthTokenServiceTests() {
        ReflectionTestUtils.setField(authTokenService, "tokenSecret", TEST_SECRET);
        ReflectionTestUtils.setField(authTokenService, "tokenIssuer", "tarkov-community-test");
        ReflectionTestUtils.setField(authTokenService, "tokenMinutes", 60L);
    }

    @Test
    void createsSignedJwtAndResolvesNormalUserToken() {
        SysUser user = user(1L, "admin", "ADMIN", "NORMAL");
        given(userMapper.selectOne(any())).willReturn(user);

        String token = authTokenService.createToken(user);
        Optional<SysUser> resolved = authTokenService.resolveUser("Bearer " + token);

        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
        assertThat(decode(parts[0])).contains("\"alg\":\"HS256\"", "\"typ\":\"JWT\"");
        assertThat(decode(parts[1])).contains("\"sub\":\"1\"", "\"username\":\"admin\"", "\"exp\"");
        assertThat(resolved).contains(user);
    }

    @Test
    void rejectsInvalidToken() {
        Optional<SysUser> resolved = authTokenService.resolveUser("Bearer not-a-valid-token");

        assertThat(resolved).isEmpty();
    }

    @Test
    void rejectsTamperedJwtPayload() {
        SysUser user = user(1L, "admin", "ADMIN", "NORMAL");
        String token = authTokenService.createToken(user);
        String[] parts = token.split("\\.");
        String tamperedPayload = encode("""
                {"sub":"1","username":"hacker","role":"ADMIN","iat":1781180000,"exp":1781183600,"iss":"tarkov-community-test"}
                """);
        String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];

        Optional<SysUser> resolved = authTokenService.resolveUser("Bearer " + tamperedToken);

        assertThat(resolved).isEmpty();
        verify(userMapper, never()).selectOne(any());
    }

    @Test
    void rejectsExpiredJwt() {
        String expiredToken = signedToken("""
                {"sub":"1","username":"admin","role":"ADMIN","iat":1700000000,"exp":1700000100,"iss":"tarkov-community-test"}
                """);

        Optional<SysUser> resolved = authTokenService.resolveUser("Bearer " + expiredToken);

        assertThat(resolved).isEmpty();
        verify(userMapper, never()).selectOne(any());
    }

    @Test
    void rejectsDisabledUser() {
        SysUser user = user(2L, "pmc_rookie", "USER", "DISABLED");
        given(userMapper.selectOne(any())).willReturn(user);

        String token = authTokenService.createToken(user);
        Optional<SysUser> resolved = authTokenService.resolveUser("Bearer " + token);

        assertThat(resolved).isEmpty();
    }

    private static SysUser user(Long id, String username, String role, String status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }

    private static String signedToken(String payloadJson) {
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode(payloadJson.trim());
        String unsignedToken = header + "." + payload;
        return unsignedToken + "." + sign(unsignedToken);
    }

    private static String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(TEST_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign test token", exception);
        }
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
