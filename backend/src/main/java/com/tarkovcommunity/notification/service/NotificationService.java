package com.tarkovcommunity.notification.service;

import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.notification.dto.NotificationReadAllResponse;
import com.tarkovcommunity.notification.dto.NotificationResponse;
import com.tarkovcommunity.notification.dto.NotificationUnreadCountResponse;
import com.tarkovcommunity.user.entity.SysUser;

public interface NotificationService {
    PageResponse<NotificationResponse> listMine(SysUser user, Integer readStatus, int page, int size);

    NotificationUnreadCountResponse countUnread(SysUser user);

    NotificationResponse markRead(SysUser user, Long id);

    NotificationReadAllResponse markAllRead(SysUser user);
}
