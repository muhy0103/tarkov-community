package com.tarkovcommunity.auth;

import com.tarkovcommunity.auth.dto.EmailVerificationResponse;
import com.tarkovcommunity.auth.dto.EmailVerificationResult;
import com.tarkovcommunity.auth.dto.LoginRequest;
import com.tarkovcommunity.auth.dto.RegisterRequest;
import com.tarkovcommunity.auth.dto.RegisterResponse;
import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.auth.service.EmailVerificationService;
import com.tarkovcommunity.auth.service.impl.AuthServiceImpl;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTests {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private AuthTokenService tokenService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Test
    void registersPendingUserAndSendsVerification() {
        AuthServiceImpl service = service();
        RegisterRequest request = new RegisterRequest(
                " woods_scout ",
                "123456",
                " 森林侦察员 ",
                " woods@example.com "
        );
        given(userMapper.selectOne(any())).willReturn(null);
        willAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(9L);
            return 1;
        }).given(userMapper).insert(any(SysUser.class));
        given(emailVerificationService.createAndSendVerification(any(SysUser.class)))
                .willReturn(new EmailVerificationResult(true));

        RegisterResponse response = service.register(request);

        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).insert(userCaptor.capture());
        SysUser savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("woods_scout");
        assertThat(savedUser.getNickname()).isEqualTo("森林侦察员");
        assertThat(savedUser.getEmail()).isEqualTo("woods@example.com");
        assertThat(savedUser.getStatus()).isEqualTo("PENDING");
        assertThat(new BCryptPasswordEncoder().matches("123456", savedUser.getPassword())).isTrue();
        verify(emailVerificationService).createAndSendVerification(savedUser);
        assertThat(response.userId()).isEqualTo(9L);
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.mailSent()).isTrue();
    }

    @Test
    void rejectsPendingUserLogin() {
        AuthServiceImpl service = service();
        SysUser user = normalUser();
        user.setStatus("PENDING");
        user.setPassword(new BCryptPasswordEncoder().encode("123456"));
        given(userMapper.selectOne(any())).willReturn(user);

        assertThatThrownBy(() -> service.login(new LoginRequest("woods_scout", "123456")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
        verifyNoInteractions(tokenService);
    }

    @Test
    void verifiesEmailByDelegatingToVerificationService() {
        AuthServiceImpl service = service();
        EmailVerificationResponse expected = new EmailVerificationResponse(
                9L,
                "woods_scout",
                "woods@example.com",
                "NORMAL",
                "邮箱验证成功，现在可以登录"
        );
        given(emailVerificationService.verifyEmail("demo-token")).willReturn(expected);

        EmailVerificationResponse response = service.verifyEmail("demo-token");

        assertThat(response).isEqualTo(expected);
    }

    private AuthServiceImpl service() {
        return new AuthServiceImpl(userMapper, tokenService, emailVerificationService);
    }

    private static SysUser normalUser() {
        SysUser user = new SysUser();
        user.setId(9L);
        user.setUsername("woods_scout");
        user.setNickname("森林侦察员");
        user.setEmail("woods@example.com");
        user.setRole("USER");
        user.setStatus("NORMAL");
        user.setContribution(0);
        return user;
    }
}
