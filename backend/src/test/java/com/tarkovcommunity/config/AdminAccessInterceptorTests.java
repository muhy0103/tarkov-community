package com.tarkovcommunity.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.user.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AdminAccessInterceptorTests {

    private final AuthTokenService authTokenService = mock(AuthTokenService.class);
    private final AdminAccessInterceptor interceptor = new AdminAccessInterceptor(authTokenService, new ObjectMapper());

    @Test
    void rejectsMissingToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("请先登录");
    }

    @Test
    void rejectsNonAdminUser() throws Exception {
        String authorization = "Bearer user-token";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/users");
        request.addHeader(HttpHeaders.AUTHORIZATION, authorization);
        MockHttpServletResponse response = new MockHttpServletResponse();
        given(authTokenService.resolveUser(authorization)).willReturn(Optional.of(user("USER")));

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("需要管理员权限");
    }

    @Test
    void allowsAdminUser() throws Exception {
        String authorization = "Bearer admin-token";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/users");
        request.addHeader(HttpHeaders.AUTHORIZATION, authorization);
        MockHttpServletResponse response = new MockHttpServletResponse();
        given(authTokenService.resolveUser(authorization)).willReturn(Optional.of(user("ADMIN")));

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    private static SysUser user(String role) {
        SysUser user = new SysUser();
        user.setRole(role);
        user.setStatus("NORMAL");
        return user;
    }
}
