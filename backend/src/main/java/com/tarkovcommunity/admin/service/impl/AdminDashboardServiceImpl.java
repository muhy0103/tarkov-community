package com.tarkovcommunity.admin.service.impl;

import com.tarkovcommunity.admin.dto.AdminDashboardSummaryResponse;
import com.tarkovcommunity.admin.service.AdminDashboardService;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.meta.mapper.CategoryMapper;
import com.tarkovcommunity.tarkov.mapper.BossMapper;
import com.tarkovcommunity.tarkov.mapper.HideoutStationMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovAmmoMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovItemMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovMapMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovQuestMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovTraderMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovWeaponMapper;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final SysUserMapper userMapper;
    private final PostMapper postMapper;
    private final PostCommentMapper commentMapper;
    private final CategoryMapper categoryMapper;
    private final TarkovMapMapper mapMapper;
    private final TarkovTraderMapper traderMapper;
    private final TarkovQuestMapper questMapper;
    private final TarkovItemMapper itemMapper;
    private final TarkovWeaponMapper weaponMapper;
    private final TarkovAmmoMapper ammoMapper;
    private final HideoutStationMapper hideoutStationMapper;
    private final BossMapper bossMapper;

    @Override
    public AdminDashboardSummaryResponse getSummary() {
        return new AdminDashboardSummaryResponse(
                userMapper.selectCount(null),
                postMapper.selectCount(null),
                commentMapper.selectCount(null),
                categoryMapper.selectCount(null),
                mapMapper.selectCount(null),
                traderMapper.selectCount(null),
                questMapper.selectCount(null),
                itemMapper.selectCount(null),
                weaponMapper.selectCount(null),
                ammoMapper.selectCount(null),
                hideoutStationMapper.selectCount(null),
                bossMapper.selectCount(null)
        );
    }
}
