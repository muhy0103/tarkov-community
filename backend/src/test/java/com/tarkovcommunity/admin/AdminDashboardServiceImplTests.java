package com.tarkovcommunity.admin;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.tarkovcommunity.admin.dto.AdminDashboardSummaryResponse;
import com.tarkovcommunity.admin.service.impl.AdminDashboardServiceImpl;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.meta.entity.Announcement;
import com.tarkovcommunity.meta.mapper.AnnouncementMapper;
import com.tarkovcommunity.meta.mapper.CategoryMapper;
import com.tarkovcommunity.moderation.entity.Report;
import com.tarkovcommunity.moderation.mapper.ReportMapper;
import com.tarkovcommunity.tarkov.mapper.BossMapper;
import com.tarkovcommunity.tarkov.mapper.HideoutStationMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovAmmoMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovItemMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovMapMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovQuestMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovTraderMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovWeaponMapper;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTests {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostCommentMapper commentMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private TarkovMapMapper mapMapper;

    @Mock
    private TarkovTraderMapper traderMapper;

    @Mock
    private TarkovQuestMapper questMapper;

    @Mock
    private TarkovItemMapper itemMapper;

    @Mock
    private TarkovWeaponMapper weaponMapper;

    @Mock
    private TarkovAmmoMapper ammoMapper;

    @Mock
    private HideoutStationMapper hideoutStationMapper;

    @Mock
    private BossMapper bossMapper;

    @Mock
    private ReportMapper reportMapper;

    @Mock
    private AnnouncementMapper announcementMapper;

    @Test
    void countsGovernanceStatusMetrics() {
        AdminDashboardServiceImpl service = service();
        given(userMapper.selectCount(isNull())).willReturn(3L);
        given(postMapper.selectCount(isNull())).willReturn(12L);
        given(commentMapper.selectCount(isNull())).willReturn(24L);
        given(categoryMapper.selectCount(isNull())).willReturn(8L);
        given(mapMapper.selectCount(isNull())).willReturn(5L);
        given(traderMapper.selectCount(isNull())).willReturn(5L);
        given(questMapper.selectCount(isNull())).willReturn(4L);
        given(itemMapper.selectCount(isNull())).willReturn(4L);
        given(weaponMapper.selectCount(isNull())).willReturn(3L);
        given(ammoMapper.selectCount(isNull())).willReturn(3L);
        given(hideoutStationMapper.selectCount(isNull())).willReturn(4L);
        given(bossMapper.selectCount(isNull())).willReturn(3L);
        given(reportMapper.selectCount(any())).willReturn(2L);
        given(announcementMapper.selectCount(any())).willReturn(6L);

        AdminDashboardSummaryResponse response = service.getSummary();

        assertThat(response.pendingReportCount()).isEqualTo(2L);
        assertThat(response.publishedAnnouncementCount()).isEqualTo(6L);

        ArgumentCaptor<Wrapper<Report>> reportWrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        ArgumentCaptor<Wrapper<Announcement>> announcementWrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(reportMapper).selectCount(reportWrapperCaptor.capture());
        verify(announcementMapper).selectCount(announcementWrapperCaptor.capture());
        assertThat(wrapperValues(reportWrapperCaptor.getValue())).containsValue("PENDING");
        assertThat(wrapperValues(announcementWrapperCaptor.getValue())).containsValue("PUBLISHED");
    }

    private AdminDashboardServiceImpl service() {
        return new AdminDashboardServiceImpl(
                userMapper,
                postMapper,
                commentMapper,
                categoryMapper,
                mapMapper,
                traderMapper,
                questMapper,
                itemMapper,
                weaponMapper,
                ammoMapper,
                hideoutStationMapper,
                bossMapper,
                reportMapper,
                announcementMapper
        );
    }

    private static java.util.Map<String, Object> wrapperValues(Wrapper<?> wrapper) {
        wrapper.getSqlSegment();
        return ((AbstractWrapper<?, ?, ?>) wrapper).getParamNameValuePairs();
    }
}
