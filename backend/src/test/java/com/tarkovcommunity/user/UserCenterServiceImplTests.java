package com.tarkovcommunity.user;

import com.tarkovcommunity.forum.mapper.FavoriteMapper;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.meta.mapper.CategoryMapper;
import com.tarkovcommunity.user.dto.UserPasswordUpdateRequest;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import com.tarkovcommunity.user.service.impl.UserCenterServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCenterServiceImplTests {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostCommentMapper postCommentMapper;

    @Mock
    private FavoriteMapper favoriteMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Test
    void changesCurrentUserPassword() {
        UserCenterServiceImpl service = service();
        SysUser user = userWithPassword("OldPass123");

        service.updatePassword(user, new UserPasswordUpdateRequest("OldPass123", "NewPass123"));

        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).updateById(userCaptor.capture());
        SysUser updatedUser = userCaptor.getValue();

        assertThat(updatedUser.getId()).isEqualTo(7L);
        assertThat(passwordEncoder.matches("NewPass123", updatedUser.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("OldPass123", updatedUser.getPassword())).isFalse();
        assertThat(updatedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void rejectsWrongCurrentPassword() {
        UserCenterServiceImpl service = service();
        SysUser user = userWithPassword("OldPass123");

        assertThatThrownBy(() -> service.updatePassword(user, new UserPasswordUpdateRequest("WrongPass123", "NewPass123")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("当前密码不正确");

        verify(sysUserMapper, never()).updateById(user);
    }

    private UserCenterServiceImpl service() {
        return new UserCenterServiceImpl(
                postMapper,
                postCommentMapper,
                favoriteMapper,
                categoryMapper,
                sysUserMapper
        );
    }

    private SysUser userWithPassword(String rawPassword) {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setUsername("pmc_rookie");
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname("Rookie");
        user.setRole("USER");
        user.setStatus("NORMAL");
        user.setContribution(18);
        return user;
    }
}
