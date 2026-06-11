package com.tarkovcommunity.auth;

import com.tarkovcommunity.auth.config.EmailVerificationProperties;
import com.tarkovcommunity.auth.dto.EmailVerificationResponse;
import com.tarkovcommunity.auth.dto.EmailVerificationResult;
import com.tarkovcommunity.auth.entity.EmailVerificationToken;
import com.tarkovcommunity.auth.mapper.EmailVerificationTokenMapper;
import com.tarkovcommunity.auth.service.EmailVerificationService;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTests {

    @Mock
    private EmailVerificationTokenMapper tokenMapper;

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private JavaMailSender mailSender;

    private EmailVerificationProperties properties;

    @BeforeEach
    void setUp() {
        properties = new EmailVerificationProperties();
        properties.setFrontendUrl("http://127.0.0.1:5173/");
        properties.setFrom("nzdyhbzx@sina.com");
        properties.setEnabled(true);
        properties.setTokenMinutes(20);
    }

    @Test
    void storesHashedTokenAndSendsMailWhenPasswordConfigured() {
        EmailVerificationService service = service("mail-password");
        SysUser user = pendingUser();

        EmailVerificationResult result = service.createAndSendVerification(user);

        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenMapper).insert(tokenCaptor.capture());
        EmailVerificationToken tokenRecord = tokenCaptor.getValue();
        assertThat(tokenRecord.getUserId()).isEqualTo(9L);
        assertThat(tokenRecord.getStatus()).isEqualTo("PENDING");
        assertThat(tokenRecord.getTokenHash()).hasSize(64).matches("[0-9a-f]+");
        assertThat(tokenRecord.getExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(4));

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());
        SimpleMailMessage mail = mailCaptor.getValue();
        assertThat(mail.getFrom()).isEqualTo("nzdyhbzx@sina.com");
        assertThat(mail.getTo()).containsExactly("woods@example.com");
        assertThat(mail.getSubject()).contains("确认你的邮箱");
        assertThat(mail.getText()).contains("森林侦察员", "/verify-email?token=");
        assertThat(result.mailSent()).isTrue();
    }

    @Test
    void rejectsRegistrationWhenMailPasswordMissing() {
        EmailVerificationService service = service("");

        assertThatThrownBy(() -> service.createAndSendVerification(pendingUser()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void rejectsRegistrationWhenMailSendFails() {
        EmailVerificationService service = service("mail-password");
        willThrow(new MailSendException("smtp unavailable"))
                .given(mailSender)
                .send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> service.createAndSendVerification(pendingUser()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void verifiesPendingTokenAndActivatesUser() {
        EmailVerificationService service = service("");
        EmailVerificationToken tokenRecord = tokenRecord("raw-token", LocalDateTime.now().plusMinutes(10));
        SysUser user = pendingUser();
        given(tokenMapper.selectOne(any())).willReturn(tokenRecord);
        given(userMapper.selectById(9L)).willReturn(user);

        EmailVerificationResponse response = service.verifyEmail(" raw-token ");

        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).updateById(userCaptor.capture());
        assertThat(userCaptor.getValue().getStatus()).isEqualTo("NORMAL");
        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenMapper).updateById(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getStatus()).isEqualTo("USED");
        assertThat(tokenCaptor.getValue().getVerifiedAt()).isNotNull();
        assertThat(response.status()).isEqualTo("NORMAL");
        assertThat(response.message()).contains("可以登录");
    }

    @Test
    void expiresOldTokenBeforeActivatingUser() {
        EmailVerificationService service = service("");
        EmailVerificationToken tokenRecord = tokenRecord("raw-token", LocalDateTime.now().minusMinutes(1));
        given(tokenMapper.selectOne(any())).willReturn(tokenRecord);

        assertThatThrownBy(() -> service.verifyEmail("raw-token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.GONE));

        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenMapper).updateById(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getStatus()).isEqualTo("EXPIRED");
        verify(userMapper, never()).selectById(any());
    }

    private EmailVerificationService service(String mailPassword) {
        EmailVerificationService service = new EmailVerificationService(
                tokenMapper,
                userMapper,
                mailSender,
                properties
        );
        ReflectionTestUtils.setField(service, "mailPassword", mailPassword);
        return service;
    }

    private static SysUser pendingUser() {
        SysUser user = new SysUser();
        user.setId(9L);
        user.setUsername("woods_scout");
        user.setNickname("森林侦察员");
        user.setEmail("woods@example.com");
        user.setStatus("PENDING");
        return user;
    }

    private static EmailVerificationToken tokenRecord(String rawToken, LocalDateTime expiresAt) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setId(3L);
        token.setUserId(9L);
        token.setTokenHash(hash(rawToken));
        token.setStatus("PENDING");
        token.setExpiresAt(expiresAt);
        return token;
    }

    private static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
