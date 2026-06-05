package com.tarkovcommunity.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminBossResponse;
import com.tarkovcommunity.admin.dto.AdminBossUpdateRequest;
import com.tarkovcommunity.admin.service.impl.AdminBossServiceImpl;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.Boss;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
import com.tarkovcommunity.tarkov.mapper.BossMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovMapMapper;
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
class AdminBossServiceImplTests {

    @Mock
    private BossMapper bossMapper;

    @Mock
    private TarkovMapMapper mapMapper;

    @Test
    void listsBossesWithMapNameAndManagementFields() {
        AdminBossServiceImpl service = new AdminBossServiceImpl(bossMapper, mapMapper);
        Page<Boss> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(boss()));
        given(bossMapper.selectPage(any(), any())).willReturn(page);
        given(mapMapper.selectBatchIds(List.of(1L))).willReturn(List.of(map()));

        PageResponse<AdminBossResponse> response = service.listBosses(1L, "ENABLED", "reshala", 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).singleElement()
                .satisfies(boss -> {
                    assertThat(boss.nameEn()).isEqualTo("Reshala");
                    assertThat(boss.nameZh()).isEqualTo("雷舍拉");
                    assertThat(boss.mapId()).isEqualTo(1L);
                    assertThat(boss.mapName()).isEqualTo("海关 / Customs");
                    assertThat(boss.description()).isEqualTo("Customs boss with multiple guards.");
                    assertThat(boss.equipmentSummary()).isEqualTo("Rifle, armor and guards.");
                });
    }

    @Test
    void updatesBossAndNormalizesOptionalText() {
        AdminBossServiceImpl service = new AdminBossServiceImpl(bossMapper, mapMapper);
        given(bossMapper.selectById(1L)).willReturn(boss(), updatedBoss());
        given(mapMapper.selectById(1L)).willReturn(map());
        given(mapMapper.selectBatchIds(List.of(1L))).willReturn(List.of(map()));

        AdminBossUpdateRequest request = new AdminBossUpdateRequest(
                " Reshala ",
                " 雷舍拉 ",
                1L,
                " Customs boss with multiple guards. ",
                " Rifle, armor and guards. ",
                "ENABLED"
        );

        AdminBossResponse response = service.updateBoss(1L, request);

        ArgumentCaptor<Boss> bossCaptor = ArgumentCaptor.forClass(Boss.class);
        verify(bossMapper).updateById(bossCaptor.capture());
        Boss savedBoss = bossCaptor.getValue();
        assertThat(savedBoss.getNameEn()).isEqualTo("Reshala");
        assertThat(savedBoss.getNameZh()).isEqualTo("雷舍拉");
        assertThat(savedBoss.getMapId()).isEqualTo(1L);
        assertThat(savedBoss.getDescription()).isEqualTo("Customs boss with multiple guards.");
        assertThat(savedBoss.getEquipmentSummary()).isEqualTo("Rifle, armor and guards.");
        assertThat(response.mapName()).isEqualTo("海关 / Customs");
    }

    private static Boss boss() {
        Boss boss = new Boss();
        boss.setId(1L);
        boss.setNameEn("Reshala");
        boss.setNameZh("雷舍拉");
        boss.setMapId(1L);
        boss.setDescription("Customs boss with multiple guards.");
        boss.setEquipmentSummary("Rifle, armor and guards.");
        boss.setStatus("ENABLED");
        return boss;
    }

    private static Boss updatedBoss() {
        return boss();
    }

    private static TarkovMap map() {
        TarkovMap map = new TarkovMap();
        map.setId(1L);
        map.setNameEn("Customs");
        map.setNameZh("海关");
        map.setStatus("ENABLED");
        return map;
    }
}
