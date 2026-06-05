package com.tarkovcommunity.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminHideoutStationResponse;
import com.tarkovcommunity.admin.dto.AdminHideoutStationUpdateRequest;
import com.tarkovcommunity.admin.service.impl.AdminHideoutStationServiceImpl;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.HideoutStation;
import com.tarkovcommunity.tarkov.mapper.HideoutStationMapper;
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
class AdminHideoutStationServiceImplTests {

    @Mock
    private HideoutStationMapper hideoutStationMapper;

    @Test
    void listsHideoutStationsWithManagementFields() {
        AdminHideoutStationServiceImpl service = new AdminHideoutStationServiceImpl(hideoutStationMapper);
        Page<HideoutStation> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(station()));
        given(hideoutStationMapper.selectPage(any(), any())).willReturn(page);

        PageResponse<AdminHideoutStationResponse> response = service.listStations("ENABLED", "med", 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).singleElement()
                .satisfies(station -> {
                    assertThat(station.nameEn()).isEqualTo("Medstation");
                    assertThat(station.nameZh()).isEqualTo("医疗站");
                    assertThat(station.description()).isEqualTo("Crafts medical supplies and injector cases.");
                    assertThat(station.status()).isEqualTo("ENABLED");
                });
    }

    @Test
    void updatesHideoutStationAndNormalizesOptionalText() {
        AdminHideoutStationServiceImpl service = new AdminHideoutStationServiceImpl(hideoutStationMapper);
        given(hideoutStationMapper.selectById(1L)).willReturn(station(), updatedStation());

        AdminHideoutStationUpdateRequest request = new AdminHideoutStationUpdateRequest(
                " Medstation ",
                " 医疗站 ",
                " Crafts medical supplies and injector cases. ",
                "ENABLED"
        );

        AdminHideoutStationResponse response = service.updateStation(1L, request);

        ArgumentCaptor<HideoutStation> stationCaptor = ArgumentCaptor.forClass(HideoutStation.class);
        verify(hideoutStationMapper).updateById(stationCaptor.capture());
        HideoutStation savedStation = stationCaptor.getValue();
        assertThat(savedStation.getNameEn()).isEqualTo("Medstation");
        assertThat(savedStation.getNameZh()).isEqualTo("医疗站");
        assertThat(savedStation.getDescription()).isEqualTo("Crafts medical supplies and injector cases.");
        assertThat(response.nameEn()).isEqualTo("Medstation");
    }

    private static HideoutStation station() {
        HideoutStation station = new HideoutStation();
        station.setId(1L);
        station.setNameEn("Medstation");
        station.setNameZh("医疗站");
        station.setDescription("Crafts medical supplies and injector cases.");
        station.setStatus("ENABLED");
        return station;
    }

    private static HideoutStation updatedStation() {
        return station();
    }
}
