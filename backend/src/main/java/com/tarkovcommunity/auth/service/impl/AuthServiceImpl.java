package com.tarkovcommunity.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tarkovcommunity.auth.dto.AuthResponse;
import com.tarkovcommunity.auth.dto.AuthUserResponse;
import com.tarkovcommunity.auth.dto.LoginRequest;
import com.tarkovcommunity.auth.dto.RegisterRequest;
import com.tarkovcommunity.auth.service.AuthService;
import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final AuthTokenService authTokenService;
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
    public AuthResponse register(RegisterRequest request) {
        if (sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.username())) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名已存在");
        }

        if (StringUtils.hasText(request.email()) && sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, request.email())) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "邮箱已被使用");
        }

        SysUser user = new SysUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setRole("USER");
        user.setStatus("NORMAL");
        user.setContribution(0);
        sysUserMapper.insert(user);

        return toAuthResponse(user);
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
