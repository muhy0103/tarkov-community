package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminAnnouncementResponse;
import com.tarkovcommunity.admin.dto.AdminAnnouncementUpsertRequest;
import com.tarkovcommunity.admin.service.AdminAnnouncementService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.meta.entity.Announcement;
import com.tarkovcommunity.meta.mapper.AnnouncementMapper;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAnnouncementServiceImpl implements AdminAnnouncementService {

    private static final int MAX_PAGE_SIZE = 50;

    private final AnnouncementMapper announcementMapper;
    private final SysUserMapper userMapper;

    @Override
    public PageResponse<AdminAnnouncementResponse> listAnnouncements(String status, String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<Announcement> query = new LambdaQueryWrapper<Announcement>()
                .orderByDesc(Announcement::getCreatedAt)
                .orderByDesc(Announcement::getId);

        if (StringUtils.hasText(status)) {
            query.eq(Announcement::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(Announcement::getTitle, keyword)
                    .or()
                    .like(Announcement::getContent, keyword));
        }

        Page<Announcement> announcementPage = announcementMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(
                safePage,
                safeSize,
                announcementPage.getTotal(),
                toResponses(announcementPage.getRecords())
        );
    }

    @Override
    public AdminAnnouncementResponse createAnnouncement(AdminAnnouncementUpsertRequest request, SysUser creator) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.title().trim());
        announcement.setContent(request.content().trim());
        announcement.setStatus(request.status());
        announcement.setCreatedBy(creator.getId());
        announcementMapper.insert(announcement);

        return toResponse(announcement, creator);
    }

    @Override
    public AdminAnnouncementResponse updateAnnouncement(Long id, AdminAnnouncementUpsertRequest request) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "公告不存在");
        }

        announcement.setTitle(request.title().trim());
        announcement.setContent(request.content().trim());
        announcement.setStatus(request.status());
        announcementMapper.updateById(announcement);

        return toResponses(List.of(announcementMapper.selectById(id))).get(0);
    }

    private List<AdminAnnouncementResponse> toResponses(List<Announcement> announcements) {
        Map<Long, SysUser> users = selectByIds(
                announcements.stream().map(Announcement::getCreatedBy).filter(Objects::nonNull).toList(),
                userMapper::selectBatchIds,
                SysUser::getId
        );

        return announcements.stream()
                .map(announcement -> toResponse(announcement, users.get(announcement.getCreatedBy())))
                .toList();
    }

    private static AdminAnnouncementResponse toResponse(Announcement announcement, SysUser creator) {
        return new AdminAnnouncementResponse(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getStatus(),
                announcement.getCreatedBy(),
                creator == null ? "未知管理员" : creator.getNickname(),
                announcement.getCreatedAt(),
                announcement.getUpdatedAt()
        );
    }

    private static <T> Map<Long, T> selectByIds(
            List<Long> ids,
            Function<List<Long>, List<T>> selector,
            Function<T, Long> idGetter
    ) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return selector.apply(ids.stream().distinct().toList())
                .stream()
                .collect(Collectors.toMap(idGetter, Function.identity()));
    }
}
