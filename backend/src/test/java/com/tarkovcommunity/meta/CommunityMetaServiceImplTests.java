package com.tarkovcommunity.meta;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.meta.dto.AnnouncementResponse;
import com.tarkovcommunity.meta.entity.Announcement;
import com.tarkovcommunity.meta.mapper.AnnouncementMapper;
import com.tarkovcommunity.meta.mapper.CategoryMapper;
import com.tarkovcommunity.meta.mapper.TagMapper;
import com.tarkovcommunity.meta.service.impl.CommunityMetaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommunityMetaServiceImplTests {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private AnnouncementMapper announcementMapper;

    @Test
    void listsPublishedAnnouncementsByLatestTime() {
        CommunityMetaServiceImpl service = new CommunityMetaServiceImpl(categoryMapper, tagMapper, announcementMapper);
        Page<Announcement> page = new Page<>(1, 5, 1);
        page.setRecords(List.of(announcement()));
        given(announcementMapper.selectPage(any(), any())).willReturn(page);

        PageResponse<AnnouncementResponse> response = service.listPublishedAnnouncements(1, 5);

        assertThat(response.records()).singleElement()
                .satisfies(announcement -> {
                    assertThat(announcement.title()).isEqualTo("社区维护提醒");
                    assertThat(announcement.content()).isEqualTo("今晚 23:00 会短暂维护。");
                    assertThat(announcement.createdAt()).isEqualTo(LocalDateTime.of(2026, 6, 5, 12, 0));
                });

        ArgumentCaptor<Page<Announcement>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(announcementMapper).selectPage(pageCaptor.capture(), any());
        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(1);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(5);
    }

    @Test
    void getsPublishedAnnouncementById() {
        CommunityMetaServiceImpl service = new CommunityMetaServiceImpl(categoryMapper, tagMapper, announcementMapper);
        given(announcementMapper.selectOne(any())).willReturn(announcement());

        AnnouncementResponse response = service.getPublishedAnnouncement(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("社区维护提醒");

        verify(announcementMapper).selectOne(any());
    }

    @Test
    void rejectsMissingPublishedAnnouncement() {
        CommunityMetaServiceImpl service = new CommunityMetaServiceImpl(categoryMapper, tagMapper, announcementMapper);
        given(announcementMapper.selectOne(any())).willReturn(null);

        assertThatThrownBy(() -> service.getPublishedAnnouncement(404L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    private static Announcement announcement() {
        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setTitle("社区维护提醒");
        announcement.setContent("今晚 23:00 会短暂维护。");
        announcement.setStatus("PUBLISHED");
        announcement.setCreatedAt(LocalDateTime.of(2026, 6, 5, 12, 0));
        announcement.setUpdatedAt(LocalDateTime.of(2026, 6, 5, 12, 10));
        return announcement;
    }

}
