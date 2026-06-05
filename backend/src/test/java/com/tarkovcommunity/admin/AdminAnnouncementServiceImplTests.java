package com.tarkovcommunity.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminAnnouncementResponse;
import com.tarkovcommunity.admin.dto.AdminAnnouncementUpsertRequest;
import com.tarkovcommunity.admin.service.impl.AdminAnnouncementServiceImpl;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.meta.entity.Announcement;
import com.tarkovcommunity.meta.mapper.AnnouncementMapper;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminAnnouncementServiceImplTests {

    @Mock
    private AnnouncementMapper announcementMapper;

    @Mock
    private SysUserMapper userMapper;

    @Test
    void listsAnnouncementsWithCreatorNickname() {
        AdminAnnouncementServiceImpl service = new AdminAnnouncementServiceImpl(announcementMapper, userMapper);
        Page<Announcement> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(announcement()));
        given(announcementMapper.selectPage(any(), any())).willReturn(page);
        given(userMapper.selectBatchIds(List.of(1L))).willReturn(List.of(admin()));

        PageResponse<AdminAnnouncementResponse> response =
                service.listAnnouncements("PUBLISHED", "维护", 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).singleElement()
                .satisfies(announcement -> {
                    assertThat(announcement.title()).isEqualTo("服务器维护提醒");
                    assertThat(announcement.creatorNickname()).isEqualTo("管理员");
                    assertThat(announcement.status()).isEqualTo("PUBLISHED");
                });
    }

    @Test
    void createsAnnouncementWithCreator() {
        AdminAnnouncementServiceImpl service = new AdminAnnouncementServiceImpl(announcementMapper, userMapper);
        SysUser admin = admin();

        AdminAnnouncementResponse response = service.createAnnouncement(new AdminAnnouncementUpsertRequest(
                " 服务器维护提醒 ",
                " 今晚 23:00 将进行社区系统维护。 ",
                "PUBLISHED"
        ), admin);

        ArgumentCaptor<Announcement> announcementCaptor = ArgumentCaptor.forClass(Announcement.class);
        verify(announcementMapper).insert(announcementCaptor.capture());
        Announcement savedAnnouncement = announcementCaptor.getValue();
        assertThat(savedAnnouncement.getTitle()).isEqualTo("服务器维护提醒");
        assertThat(savedAnnouncement.getContent()).isEqualTo("今晚 23:00 将进行社区系统维护。");
        assertThat(savedAnnouncement.getStatus()).isEqualTo("PUBLISHED");
        assertThat(savedAnnouncement.getCreatedBy()).isEqualTo(1L);
        assertThat(response.creatorNickname()).isEqualTo("管理员");
    }

    @Test
    void updatesAnnouncementAndKeepsCreator() {
        AdminAnnouncementServiceImpl service = new AdminAnnouncementServiceImpl(announcementMapper, userMapper);
        given(announcementMapper.selectById(1L)).willReturn(announcement(), updatedAnnouncement());
        given(userMapper.selectBatchIds(List.of(1L))).willReturn(List.of(admin()));

        AdminAnnouncementResponse response = service.updateAnnouncement(1L, new AdminAnnouncementUpsertRequest(
                " 社区规则更新 ",
                " 新增组队招募发帖规范。 ",
                "DRAFT"
        ));

        ArgumentCaptor<Announcement> announcementCaptor = ArgumentCaptor.forClass(Announcement.class);
        verify(announcementMapper).updateById(announcementCaptor.capture());
        Announcement savedAnnouncement = announcementCaptor.getValue();
        assertThat(savedAnnouncement.getTitle()).isEqualTo("社区规则更新");
        assertThat(savedAnnouncement.getContent()).isEqualTo("新增组队招募发帖规范。");
        assertThat(savedAnnouncement.getStatus()).isEqualTo("DRAFT");
        assertThat(savedAnnouncement.getCreatedBy()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("社区规则更新");
        assertThat(response.creatorNickname()).isEqualTo("管理员");
    }

    private static Announcement announcement() {
        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setTitle("服务器维护提醒");
        announcement.setContent("今晚 23:00 将进行社区系统维护。");
        announcement.setStatus("PUBLISHED");
        announcement.setCreatedBy(1L);
        announcement.setCreatedAt(LocalDateTime.of(2026, 6, 5, 12, 0));
        announcement.setUpdatedAt(LocalDateTime.of(2026, 6, 5, 12, 10));
        return announcement;
    }

    private static Announcement updatedAnnouncement() {
        Announcement announcement = announcement();
        announcement.setTitle("社区规则更新");
        announcement.setContent("新增组队招募发帖规范。");
        announcement.setStatus("DRAFT");
        announcement.setUpdatedAt(LocalDateTime.of(2026, 6, 5, 12, 30));
        return announcement;
    }

    private static SysUser admin() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setNickname("管理员");
        user.setRole("ADMIN");
        user.setStatus("NORMAL");
        return user;
    }
}
