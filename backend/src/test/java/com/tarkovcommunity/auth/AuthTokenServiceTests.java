package com.tarkovcommunity.auth;

import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AuthTokenServiceTests {

    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final AuthTokenService authTokenService = new AuthTokenService(userMapper);

    @Test
    void createsAndResolvesNormalUserToken() {
        SysUser user = user(1L, "admin", "ADMIN", "NORMAL");
        given(userMapper.selectOne(any())).willReturn(user);

        String token = authTokenService.createToken(user);
        Optional<SysUser> resolved = authTokenService.resolveUser("Bearer " + token);

        assertThat(resolved).contains(user);
    }

    @Test
    void rejectsInvalidToken() {
        Optional<SysUser> resolved = authTokenService.resolveUser("Bearer not-a-valid-token");

        assertThat(resolved).isEmpty();
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
}
