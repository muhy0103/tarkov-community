package com.tarkovcommunity.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminMapResponse;
import com.tarkovcommunity.admin.dto.AdminMapUpdateRequest;
import com.tarkovcommunity.admin.service.impl.AdminMapServiceImpl;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
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
class AdminMapServiceImplTests {

    private static final String IMAGE_URL = "https://assets.tarkov.dev/customs.webp";

    @Mock
    private TarkovMapMapper mapMapper;

    @Test
    void listsMapsWithImageUrl() {
        AdminMapServiceImpl service = new AdminMapServiceImpl(mapMapper);
        Page<TarkovMap> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(map()));
        given(mapMapper.selectPage(any(), any())).willReturn(page);

        PageResponse<AdminMapResponse> response = service.listMaps("ENABLED", "customs", 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).singleElement()
                .satisfies(map -> {
                    assertThat(map.nameEn()).isEqualTo("Customs");
                    assertThat(map.imageUrl()).isEqualTo(IMAGE_URL);
                });
    }

    @Test
    void updatesMapAndNormalizesImageUrl() {
        AdminMapServiceImpl service = new AdminMapServiceImpl(mapMapper);
        given(mapMapper.selectById(1L)).willReturn(map(), updatedMap());

        AdminMapUpdateRequest request = new AdminMapUpdateRequest(
                " Customs ",
                " Customs ",
                " Medium ",
                " Industrial area and dorms. ",
                "15+",
                " " + IMAGE_URL + " ",
                "ENABLED"
        );

        AdminMapResponse response = service.updateMap(1L, request);

        ArgumentCaptor<TarkovMap> mapCaptor = ArgumentCaptor.forClass(TarkovMap.class);
        verify(mapMapper).updateById(mapCaptor.capture());
        TarkovMap savedMap = mapCaptor.getValue();
        assertThat(savedMap.getImageUrl()).isEqualTo(IMAGE_URL);
        assertThat(response.imageUrl()).isEqualTo(IMAGE_URL);
    }

    private static TarkovMap map() {
        TarkovMap map = new TarkovMap();
        map.setId(1L);
        map.setNameEn("Customs");
        map.setNameZh("Customs");
        map.setDifficulty("Medium");
        map.setDescription("Industrial area and dorms.");
        map.setRecommendedLevel("15+");
        map.setImageUrl(IMAGE_URL);
        map.setStatus("ENABLED");
        return map;
    }

    private static TarkovMap updatedMap() {
        return map();
    }
}
