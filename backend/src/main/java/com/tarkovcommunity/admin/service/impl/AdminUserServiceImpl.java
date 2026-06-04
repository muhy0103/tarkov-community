package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminUserResponse;
import com.tarkovcommunity.admin.dto.AdminUserUpdateRequest;
import com.tarkovcommunity.admin.service.AdminUserService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "MODERATOR", "ADMIN");
    private static final Set<String> ALLOWED_STATUSES = Set.of("NORMAL", "DISABLED", "BANNED");

    private final SysUserMapper userMapper;

    @Override
    public PageResponse<AdminUserResponse> listUsers(
            String role,
            String status,
            String keyword,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>()
                .orderByDesc(SysUser::getCreatedAt)
                .orderByDesc(SysUser::getId);

        if (StringUtils.hasText(role)) {
            query.eq(SysUser::getRole, role);
        }

        if (StringUtils.hasText(status)) {
            query.eq(SysUser::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getNickname, keyword)
                    .or()
                    .like(SysUser::getEmail, keyword));
        }

        Page<SysUser> userPage = userMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, userPage.getTotal(), toResponses(userPage.getRecords()));
    }

    @Override
    public AdminUserResponse updateUser(Long id, AdminUserUpdateRequest request) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }

        boolean changed = false;
        if (StringUtils.hasText(request.role())) {
            if (!ALLOWED_ROLES.contains(request.role())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户角色不正确");
            }
            user.setRole(request.role());
            changed = true;
        }

        if (StringUtils.hasText(request.status())) {
            if (!ALLOWED_STATUSES.contains(request.status())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户状态不正确");
            }
            user.setStatus(request.status());
            changed = true;
        }

        if (!changed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "至少需要更新角色或状态");
        }

        userMapper.updateById(user);
        return toResponse(userMapper.selectById(id));
    }

    private static List<AdminUserResponse> toResponses(List<SysUser> users) {
        return users.stream()
                .map(AdminUserServiceImpl::toResponse)
                .toList();
    }

    private static AdminUserResponse toResponse(SysUser user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getContribution(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
