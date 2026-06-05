package com.tarkovcommunity.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminMapLootAreaResponse;
import com.tarkovcommunity.admin.dto.AdminMapLootAreaUpdateRequest;
import com.tarkovcommunity.admin.service.impl.AdminMapLootAreaServiceImpl;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.MapLootArea;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
import com.tarkovcommunity.tarkov.mapper.MapLootAreaMapper;
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
class AdminMapLootAreaServiceImplTests {

    @Mock
    private MapLootAreaMapper mapLootAreaMapper;

    @Mock
    private TarkovMapMapper mapMapper;

    @Test
    void listsMapLootAreasWithMapName() {
        AdminMapLootAreaServiceImpl service = new AdminMapLootAreaServiceImpl(mapLootAreaMapper, mapMapper);
        Page<MapLootArea> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(lootArea()));
        given(mapLootAreaMapper.selectPage(any(), any())).willReturn(page);
        given(mapMapper.selectBatchIds(List.of(1L))).willReturn(List.of(map()));

        PageResponse<AdminMapLootAreaResponse> response =
                service.listLootAreas(1L, "HIGH", "Dorms", 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).singleElement()
                .satisfies(lootArea -> {
                    assertThat(lootArea.mapName()).isEqualTo("海关 / Customs");
                    assertThat(lootArea.name()).isEqualTo("Dorms Marked Room");
                    assertThat(lootArea.lootType()).isEqualTo("High-value room");
                    assertThat(lootArea.riskLevel()).isEqualTo("HIGH");
                    assertThat(lootArea.description()).isEqualTo("Marked key room with frequent player traffic.");
                });
    }

    @Test
    void updatesMapLootAreaAndNormalizesText() {
        AdminMapLootAreaServiceImpl service = new AdminMapLootAreaServiceImpl(mapLootAreaMapper, mapMapper);
        given(mapLootAreaMapper.selectById(1L)).willReturn(lootArea(), updatedLootArea());
        given(mapMapper.selectById(1L)).willReturn(map());
        given(mapMapper.selectBatchIds(List.of(1L))).willReturn(List.of(map()));

        AdminMapLootAreaUpdateRequest request = new AdminMapLootAreaUpdateRequest(
                1L,
                " Dorms Marked Room ",
                " High-value room ",
                " HIGH ",
                " Marked key room with frequent player traffic. "
        );

        AdminMapLootAreaResponse response = service.updateLootArea(1L, request);

        ArgumentCaptor<MapLootArea> lootAreaCaptor = ArgumentCaptor.forClass(MapLootArea.class);
        verify(mapLootAreaMapper).updateById(lootAreaCaptor.capture());
        MapLootArea savedLootArea = lootAreaCaptor.getValue();
        assertThat(savedLootArea.getMapId()).isEqualTo(1L);
        assertThat(savedLootArea.getName()).isEqualTo("Dorms Marked Room");
        assertThat(savedLootArea.getLootType()).isEqualTo("High-value room");
        assertThat(savedLootArea.getRiskLevel()).isEqualTo("HIGH");
        assertThat(savedLootArea.getDescription()).isEqualTo("Marked key room with frequent player traffic.");
        assertThat(response.mapName()).isEqualTo("海关 / Customs");
    }

    private static MapLootArea lootArea() {
        MapLootArea lootArea = new MapLootArea();
        lootArea.setId(1L);
        lootArea.setMapId(1L);
        lootArea.setName("Dorms Marked Room");
        lootArea.setLootType("High-value room");
        lootArea.setRiskLevel("HIGH");
        lootArea.setDescription("Marked key room with frequent player traffic.");
        return lootArea;
    }

    private static MapLootArea updatedLootArea() {
        return lootArea();
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
