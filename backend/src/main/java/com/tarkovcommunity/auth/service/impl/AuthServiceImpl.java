package com.tarkovcommunity.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tarkovcommunity.auth.dto.AuthResponse;
import com.tarkovcommunity.auth.dto.AuthUserResponse;
import com.tarkovcommunity.auth.dto.EmailVerificationResponse;
import com.tarkovcommunity.auth.dto.EmailVerificationResult;
import com.tarkovcommunity.auth.dto.LoginRequest;
import com.tarkovcommunity.auth.dto.RegisterRequest;
import com.tarkovcommunity.auth.dto.RegisterResponse;
import com.tarkovcommunity.auth.service.AuthService;
import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.auth.service.EmailVerificationService;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final AuthTokenService authTokenService;
    private final EmailVerificationService emailVerificationService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AuthResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.username()));

        if (user == null || !"NORMAL".equals(user.getStatus()) || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        return toAuthResponse(user);
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String nickname = request.nickname().trim();
        String email = request.email().trim();

        if (sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名已存在");
        }

        if (StringUtils.hasText(email) && sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email)) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "邮箱已被使用");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(nickname);
        user.setEmail(email);
        user.setRole("USER");
        user.setStatus("PENDING");
        user.setContribution(0);
        sysUserMapper.insert(user);

        EmailVerificationResult verification = emailVerificationService.createAndSendVerification(user);
        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus(),
                "\u6ce8\u518c\u6210\u529f\uff0c\u8bf7\u524d\u5f80\u90ae\u7bb1\u70b9\u51fb\u786e\u8ba4\u94fe\u63a5",
                verification.mailSent()
        );
    }

    @Override
    public EmailVerificationResponse verifyEmail(String token) {
        return emailVerificationService.verifyEmail(token);
    }

    private AuthResponse toAuthResponse(SysUser user) {
        return new AuthResponse(
                authTokenService.createToken(user),
                new AuthUserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getNickname(),
                        user.getRole(),
                        user.getContribution()
                )
        );
    }
}
