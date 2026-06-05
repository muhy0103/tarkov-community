package com.tarkovcommunity.notification;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.notification.dto.NotificationResponse;
import com.tarkovcommunity.notification.entity.Notification;
import com.tarkovcommunity.notification.mapper.NotificationMapper;
import com.tarkovcommunity.notification.service.impl.NotificationServiceImpl;
import com.tarkovcommunity.user.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTests {

    @Mock
    private NotificationMapper notificationMapper;

    @Test
    void listsCurrentUserNotificationsWithReadFilter() {
        NotificationServiceImpl service = new NotificationServiceImpl(notificationMapper);
        Page<Notification> notificationPage = new Page<>(1, 10, 1);
        notificationPage.setRecords(List.of(notification(11L, 7L, 0)));
        given(notificationMapper.selectPage(any(), any())).willReturn(notificationPage);

        var response = service.listMine(user(), 0, 1, 10);

        assertThat(response.total()).isEqualTo(1L);
        assertThat(response.records()).hasSize(1);
        NotificationResponse notification = response.records().get(0);
        assertThat(notification.id()).isEqualTo(11L);
        assertThat(notification.readStatus()).isEqualTo(0);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Wrapper<Notification>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(notificationMapper).selectPage(any(), wrapperCaptor.capture());
        assertThat(wrapperValues(wrapperCaptor.getValue())).containsEntry("MPGENVAL1", 7L);
        assertThat(wrapperValues(wrapperCaptor.getValue())).containsEntry("MPGENVAL2", 0);
    }

    @Test
    void countsUnreadNotificationsForCurrentUser() {
        NotificationServiceImpl service = new NotificationServiceImpl(notificationMapper);
        given(notificationMapper.selectCount(any())).willReturn(3L);

        var response = service.countUnread(user());

        assertThat(response.unreadCount()).isEqualTo(3L);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Wrapper<Notification>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(notificationMapper).selectCount(wrapperCaptor.capture());
        assertThat(wrapperValues(wrapperCaptor.getValue())).containsEntry("MPGENVAL1", 7L);
        assertThat(wrapperValues(wrapperCaptor.getValue())).containsEntry("MPGENVAL2", 0);
    }

    @Test
    void marksOwnedNotificationAsRead() {
        NotificationServiceImpl service = new NotificationServiceImpl(notificationMapper);
        given(notificationMapper.selectById(11L)).willReturn(notification(11L, 7L, 0));

        NotificationResponse response = service.markRead(user(), 11L);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationMapper).updateById(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().getReadStatus()).isEqualTo(1);
        assertThat(response.readStatus()).isEqualTo(1);
    }

    @Test
    void rejectsReadingOtherUsersNotification() {
        NotificationServiceImpl service = new NotificationServiceImpl(notificationMapper);
        given(notificationMapper.selectById(11L)).willReturn(notification(11L, 8L, 0));

        assertThatThrownBy(() -> service.markRead(user(), 11L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
        verify(notificationMapper, never()).updateById(any(Notification.class));
    }

    private static Notification notification(Long id, Long userId, Integer readStatus) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUserId(userId);
        notification.setType("REPORT_RESULT");
        notification.setTitle("Report handled");
        notification.setContent("Your report has been reviewed.");
        notification.setRelatedId(31L);
        notification.setReadStatus(readStatus);
        notification.setCreatedAt(LocalDateTime.of(2026, 6, 5, 18, 0));
        return notification;
    }

    private static SysUser user() {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setUsername("pmc_rookie");
        user.setRole("USER");
        user.setStatus("NORMAL");
        return user;
    }

    private static java.util.Map<String, Object> wrapperValues(Wrapper<?> wrapper) {
        wrapper.getSqlSegment();
        return ((AbstractWrapper<?, ?, ?>) wrapper).getParamNameValuePairs();
    }
}
