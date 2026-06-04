package com.tarkovcommunity.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.user.entity.SysUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@ConditionalOnBean(AuthTokenService.class)
@RequiredArgsConstructor
public class AdminAccessInterceptor implements HandlerInterceptor {

    private final AuthTokenService authTokenService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Optional<SysUser> user = authTokenService.resolveUser(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (user.isEmpty()) {
            writeError(response, HttpStatus.UNAUTHORIZED, "请先登录");
            return false;
        }

        if (!"ADMIN".equals(user.get().getRole())) {
            writeError(response, HttpStatus.FORBIDDEN, "需要管理员权限");
            return false;
        }

        return true;
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(status.value(), message)));
    }
}
