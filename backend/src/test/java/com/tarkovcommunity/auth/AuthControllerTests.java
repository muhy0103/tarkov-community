package com.tarkovcommunity.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.auth.controller.AuthController;
import com.tarkovcommunity.auth.dto.AuthResponse;
import com.tarkovcommunity.auth.dto.AuthUserResponse;
import com.tarkovcommunity.auth.dto.EmailVerificationResponse;
import com.tarkovcommunity.auth.dto.LoginRequest;
import com.tarkovcommunity.auth.dto.RegisterRequest;
import com.tarkovcommunity.auth.dto.RegisterResponse;
import com.tarkovcommunity.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void logsInUser() throws Exception {
        LoginRequest request = new LoginRequest("pmc_rookie", "123456");
        AuthResponse response = new AuthResponse(
                "demo-token",
                new AuthUserResponse(2L, "pmc_rookie", "海关萌新", "USER", 12)
        );

        given(authService.login(any(LoginRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("demo-token"))
                .andExpect(jsonPath("$.data.user.username").value("pmc_rookie"));
    }

    @Test
    void registersUser() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "woods_scout",
                "123456",
                "森林侦察员",
                "woods@example.com"
        );
        RegisterResponse response = new RegisterResponse(
                9L,
                "woods_scout",
                "woods@example.com",
                "PENDING",
                "注册成功，请前往邮箱点击确认链接",
                true
        );

        given(authService.register(any(RegisterRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("woods_scout"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.mailSent").value(true))
                .andExpect(jsonPath("$.data.devVerificationUrl").doesNotExist());
    }

    @Test
    void verifiesEmailToken() throws Exception {
        EmailVerificationResponse response = new EmailVerificationResponse(
                9L,
                "woods_scout",
                "woods@example.com",
                "NORMAL",
                "邮箱验证成功，现在可以登录"
        );

        given(authService.verifyEmail("demo-token")).willReturn(response);

        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", "demo-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("woods_scout"))
                .andExpect(jsonPath("$.data.status").value("NORMAL"));
    }

    @Test
    void rejectsInvalidLoginRequest() throws Exception {
        LoginRequest request = new LoginRequest("", "1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void rejectsRegisterRequestWithoutEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "woods_scout",
                                  "password": "123456",
                                  "nickname": "森林侦察员",
                                  "email": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("邮箱不能为空"));
    }
}
