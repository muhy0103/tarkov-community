package com.tarkovcommunity.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminMapExtractResponse;
import com.tarkovcommunity.admin.dto.AdminMapExtractUpdateRequest;
import com.tarkovcommunity.admin.service.impl.AdminMapExtractServiceImpl;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.MapExtract;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
import com.tarkovcommunity.tarkov.mapper.MapExtractMapper;
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
class AdminMapExtractServiceImplTests {

    @Mock
    private MapExtractMapper mapExtractMapper;

    @Mock
    private TarkovMapMapper mapMapper;

    @Test
    void listsMapExtractsWithMapName() {
        AdminMapExtractServiceImpl service = new AdminMapExtractServiceImpl(mapExtractMapper, mapMapper);
        Page<MapExtract> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(extract()));
        given(mapExtractMapper.selectPage(any(), any())).willReturn(page);
        given(mapMapper.selectBatchIds(List.of(1L))).willReturn(List.of(map()));

        PageResponse<AdminMapExtractResponse> response = service.listExtracts(1L, "ENABLED", "RUAF", 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).singleElement()
                .satisfies(extract -> {
                    assertThat(extract.mapName()).isEqualTo("海关 / Customs");
                    assertThat(extract.name()).isEqualTo("RUAF Roadblock");
                    assertThat(extract.factionLimit()).isEqualTo("All");
                    assertThat(extract.conditionText()).isEqualTo("Green flare active");
                    assertThat(extract.status()).isEqualTo("ENABLED");
                });
    }

    @Test
    void updatesMapExtractAndNormalizesText() {
        AdminMapExtractServiceImpl service = new AdminMapExtractServiceImpl(mapExtractMapper, mapMapper);
        given(mapExtractMapper.selectById(1L)).willReturn(extract(), updatedExtract());
        given(mapMapper.selectById(1L)).willReturn(map());
        given(mapMapper.selectBatchIds(List.of(1L))).willReturn(List.of(map()));

        AdminMapExtractUpdateRequest request = new AdminMapExtractUpdateRequest(
                1L,
                " RUAF Roadblock ",
                " All ",
                " Green flare active ",
                " A vehicle-side checkpoint extract on Customs. ",
                "ENABLED"
        );

        AdminMapExtractResponse response = service.updateExtract(1L, request);

        ArgumentCaptor<MapExtract> extractCaptor = ArgumentCaptor.forClass(MapExtract.class);
        verify(mapExtractMapper).updateById(extractCaptor.capture());
        MapExtract savedExtract = extractCaptor.getValue();
        assertThat(savedExtract.getMapId()).isEqualTo(1L);
        assertThat(savedExtract.getName()).isEqualTo("RUAF Roadblock");
        assertThat(savedExtract.getFactionLimit()).isEqualTo("All");
        assertThat(savedExtract.getConditionText()).isEqualTo("Green flare active");
        assertThat(savedExtract.getDescription()).isEqualTo("A vehicle-side checkpoint extract on Customs.");
        assertThat(savedExtract.getStatus()).isEqualTo("ENABLED");
        assertThat(response.mapName()).isEqualTo("海关 / Customs");
    }

    private static MapExtract extract() {
        MapExtract extract = new MapExtract();
        extract.setId(1L);
        extract.setMapId(1L);
        extract.setName("RUAF Roadblock");
        extract.setFactionLimit("All");
        extract.setConditionText("Green flare active");
        extract.setDescription("A vehicle-side checkpoint extract on Customs.");
        extract.setStatus("ENABLED");
        return extract;
    }

    private static MapExtract updatedExtract() {
        return extract();
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
