package com.tarkovcommunity.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final SysUserMapper userMapper;

    public String createToken(SysUser user) {
        String payload = user.getId() + ":" + user.getUsername() + ":" + Instant.now();
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<SysUser> resolveUser(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }

        try {
            String payload = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = payload.split(":", 3);
            if (parts.length < 2) {
                return Optional.empty();
            }

            Long userId = Long.valueOf(parts[0]);
            String username = parts[1];
            SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getId, userId)
                    .eq(SysUser::getUsername, username));

            if (user == null || !"NORMAL".equals(user.getStatus())) {
                return Optional.empty();
            }

            return Optional.of(user);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
