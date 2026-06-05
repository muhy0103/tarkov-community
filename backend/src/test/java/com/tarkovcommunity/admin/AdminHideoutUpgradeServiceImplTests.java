package com.tarkovcommunity.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminHideoutUpgradeResponse;
import com.tarkovcommunity.admin.dto.AdminHideoutUpgradeUpdateRequest;
import com.tarkovcommunity.admin.service.impl.AdminHideoutUpgradeServiceImpl;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.HideoutStation;
import com.tarkovcommunity.tarkov.entity.HideoutUpgrade;
import com.tarkovcommunity.tarkov.mapper.HideoutStationMapper;
import com.tarkovcommunity.tarkov.mapper.HideoutUpgradeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminHideoutUpgradeServiceImplTests {

    @Mock
    private HideoutUpgradeMapper hideoutUpgradeMapper;

    @Mock
    private HideoutStationMapper hideoutStationMapper;

    @Test
    void listsHideoutUpgradesWithStationName() {
        AdminHideoutUpgradeServiceImpl service = new AdminHideoutUpgradeServiceImpl(hideoutUpgradeMapper, hideoutStationMapper);
        Page<HideoutUpgrade> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(upgrade()));
        given(hideoutUpgradeMapper.selectPage(any(), any())).willReturn(page);
        given(hideoutStationMapper.selectBatchIds(List.of(1L))).willReturn(List.of(station()));

        PageResponse<AdminHideoutUpgradeResponse> response = service.listUpgrades(1L, "toolset", 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).singleElement()
                .satisfies(upgrade -> {
                    assertThat(upgrade.stationName()).isEqualTo("工作台 / Workbench");
                    assertThat(upgrade.level()).isEqualTo(2);
                    assertThat(upgrade.requiredItems()).isEqualTo("Toolset x1, Bolts x5");
                    assertThat(upgrade.requiredTime()).isEqualTo("2h");
                    assertThat(upgrade.unlocks()).isEqualTo("Unlocks mid-tier weapon crafts");
                });
    }

    @Test
    void updatesHideoutUpgradeAndNormalizesText() {
        AdminHideoutUpgradeServiceImpl service = new AdminHideoutUpgradeServiceImpl(hideoutUpgradeMapper, hideoutStationMapper);
        given(hideoutUpgradeMapper.selectById(1L)).willReturn(upgrade(), updatedUpgrade());
        given(hideoutStationMapper.selectById(1L)).willReturn(station());
        given(hideoutStationMapper.selectBatchIds(List.of(1L))).willReturn(List.of(station()));

        AdminHideoutUpgradeUpdateRequest request = new AdminHideoutUpgradeUpdateRequest(
                1L,
                2,
                " Toolset x1, Bolts x5 ",
                " 2h ",
                " Unlocks mid-tier weapon crafts "
        );

        AdminHideoutUpgradeResponse response = service.updateUpgrade(1L, request);

        ArgumentCaptor<HideoutUpgrade> upgradeCaptor = ArgumentCaptor.forClass(HideoutUpgrade.class);
        verify(hideoutUpgradeMapper).updateById(upgradeCaptor.capture());
        HideoutUpgrade savedUpgrade = upgradeCaptor.getValue();
        assertThat(savedUpgrade.getStationId()).isEqualTo(1L);
        assertThat(savedUpgrade.getLevel()).isEqualTo(2);
        assertThat(savedUpgrade.getRequiredItems()).isEqualTo("Toolset x1, Bolts x5");
        assertThat(savedUpgrade.getRequiredTime()).isEqualTo("2h");
        assertThat(savedUpgrade.getUnlocks()).isEqualTo("Unlocks mid-tier weapon crafts");
        assertThat(response.stationName()).isEqualTo("工作台 / Workbench");
    }

    private static HideoutUpgrade upgrade() {
        HideoutUpgrade upgrade = new HideoutUpgrade();
        upgrade.setId(1L);
        upgrade.setStationId(1L);
        upgrade.setLevel(2);
        upgrade.setRequiredItems("Toolset x1, Bolts x5");
        upgrade.setRequiredTime("2h");
        upgrade.setUnlocks("Unlocks mid-tier weapon crafts");
        return upgrade;
    }

    private static HideoutUpgrade updatedUpgrade() {
        return upgrade();
    }

    private static HideoutStation station() {
        HideoutStation station = new HideoutStation();
        station.setId(1L);
        station.setNameEn("Workbench");
        station.setNameZh("工作台");
        station.setStatus("ENABLED");
        return station;
    }
}
