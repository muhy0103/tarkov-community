package com.tarkovcommunity.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.notification.dto.NotificationReadAllResponse;
import com.tarkovcommunity.notification.dto.NotificationResponse;
import com.tarkovcommunity.notification.dto.NotificationUnreadCountResponse;
import com.tarkovcommunity.notification.entity.Notification;
import com.tarkovcommunity.notification.mapper.NotificationMapper;
import com.tarkovcommunity.notification.service.NotificationService;
import com.tarkovcommunity.user.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int UNREAD = 0;
    private static final int READ = 1;

    private final NotificationMapper notificationMapper;

    @Override
    public PageResponse<NotificationResponse> listMine(SysUser user, Integer readStatus, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        QueryWrapper<Notification> query = new QueryWrapper<Notification>()
                .eq("user_id", user.getId())
                .eq(readStatus != null, "read_status", readStatus)
                .orderByAsc("read_status")
                .orderByDesc("created_at")
                .orderByDesc("id");

        Page<Notification> notificationPage = notificationMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(
                safePage,
                safeSize,
                notificationPage.getTotal(),
                notificationPage.getRecords().stream().map(this::toResponse).toList()
        );
    }

    @Override
    public NotificationUnreadCountResponse countUnread(SysUser user) {
        Long count = notificationMapper.selectCount(new QueryWrapper<Notification>()
                .eq("user_id", user.getId())
                .eq("read_status", UNREAD));
        return new NotificationUnreadCountResponse(count);
    }

    @Override
    public NotificationResponse markRead(SysUser user, Long id) {
        Notification notification = requireOwnedNotification(user, id);
        if (notification.getReadStatus() == null || notification.getReadStatus() != READ) {
            notification.setReadStatus(READ);
            notificationMapper.updateById(notification);
        }
        return toResponse(notification);
    }

    @Override
    public NotificationReadAllResponse markAllRead(SysUser user) {
        int updated = notificationMapper.update(
                null,
                new UpdateWrapper<Notification>()
                        .eq("user_id", user.getId())
                        .eq("read_status", UNREAD)
                        .set("read_status", READ)
        );
        return new NotificationReadAllResponse(updated);
    }

    private Notification requireOwnedNotification(SysUser user, Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null || !user.getId().equals(notification.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "通知不存在");
        }
        return notification;
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getRelatedId(),
                notification.getReadStatus(),
                notification.getCreatedAt()
        );
    }
}
