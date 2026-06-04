package com.tarkovcommunity.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.auth.controller.AuthController;
import com.tarkovcommunity.auth.dto.AuthResponse;
import com.tarkovcommunity.auth.dto.AuthUserResponse;
import com.tarkovcommunity.auth.dto.LoginRequest;
import com.tarkovcommunity.auth.dto.RegisterRequest;
import com.tarkovcommunity.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
        AuthResponse response = new AuthResponse(
                "register-token",
                new AuthUserResponse(9L, "woods_scout", "森林侦察员", "USER", 0)
        );

        given(authService.register(any(RegisterRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.user.nickname").value("森林侦察员"));
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
}
