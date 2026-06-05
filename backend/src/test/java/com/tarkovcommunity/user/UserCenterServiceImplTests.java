package com.tarkovcommunity.user;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.forum.entity.PostComment;
import com.tarkovcommunity.forum.mapper.FavoriteMapper;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.meta.mapper.CategoryMapper;
import com.tarkovcommunity.user.dto.UserCenterSummaryResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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

    @Test
    void countsAndListsOnlyNormalComments() {
        UserCenterServiceImpl service = service();
        SysUser user = userWithPassword("OldPass123");
        given(postMapper.selectCount(any())).willReturn(2L);
        given(postCommentMapper.selectCount(any())).willReturn(5L);
        given(favoriteMapper.selectCount(any())).willReturn(1L);
        given(postCommentMapper.selectPage(any(), any())).willReturn(new Page<PostComment>(1, 10, 0));

        UserCenterSummaryResponse summary = service.getSummary(user);
        service.listComments(user, 1, 10);

        assertThat(summary.commentCount()).isEqualTo(5L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Wrapper<PostComment>> countWrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Wrapper<PostComment>> pageWrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postCommentMapper).selectCount(countWrapperCaptor.capture());
        verify(postCommentMapper).selectPage(any(), pageWrapperCaptor.capture());
        assertThat(wrapperValues(countWrapperCaptor.getValue())).containsEntry("MPGENVAL1", 7L);
        assertThat(wrapperValues(countWrapperCaptor.getValue())).containsValue("NORMAL");
        assertThat(wrapperValues(pageWrapperCaptor.getValue())).containsEntry("MPGENVAL1", 7L);
        assertThat(wrapperValues(pageWrapperCaptor.getValue())).containsValue("NORMAL");
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

    private static java.util.Map<String, Object> wrapperValues(Wrapper<?> wrapper) {
        wrapper.getSqlSegment();
        return ((AbstractWrapper<?, ?, ?>) wrapper).getParamNameValuePairs();
    }
}
