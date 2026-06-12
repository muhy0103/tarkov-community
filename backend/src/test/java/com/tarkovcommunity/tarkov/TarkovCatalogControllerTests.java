package com.tarkovcommunity.tarkov;

import com.tarkovcommunity.tarkov.controller.TarkovCatalogController;
import com.tarkovcommunity.tarkov.dto.HideoutStationDetailResponse;
import com.tarkovcommunity.tarkov.dto.HideoutUpgradeResponse;
import com.tarkovcommunity.tarkov.dto.MapExtractResponse;
import com.tarkovcommunity.tarkov.dto.MapLootAreaResponse;
import com.tarkovcommunity.tarkov.dto.TarkovMapResponse;
import com.tarkovcommunity.tarkov.dto.TarkovMapDetailResponse;
import com.tarkovcommunity.tarkov.dto.TraderResponse;
import com.tarkovcommunity.tarkov.service.TarkovCatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TarkovCatalogController.class)
class TarkovCatalogControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TarkovCatalogService tarkovCatalogService;

    @Test
    void listsMaps() throws Exception {
        given(tarkovCatalogService.listMaps())
                .willReturn(List.of(new TarkovMapResponse(1L, "Customs", "海关", "中等", "1+")));

        mockMvc.perform(get("/api/tarkov/maps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].nameEn").value("Customs"));
    }

    @Test
    void listsTraders() throws Exception {
        given(tarkovCatalogService.listTraders())
                .willReturn(List.of(new TraderResponse(1L, "Prapor", null, "默认解锁")));

        mockMvc.perform(get("/api/tarkov/traders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].nameEn").value("Prapor"))
                .andExpect(jsonPath("$.data[0].nameZh").doesNotExist());
    }

    @Test
    void getsMapDetail() throws Exception {
        given(tarkovCatalogService.getMapDetail(1L))
                .willReturn(new TarkovMapDetailResponse(
                        1L,
                        "Customs",
                        "海关",
                        "中等",
                        "任务密集、撤离选择多。",
                        "1+",
                        List.of(new MapExtractResponse(1L, "Crossroads", "SCAV/PMC", "默认开启", "西侧撤离点")),
                        List.of(new MapLootAreaResponse(1L, "宿舍楼", "钥匙房/保险箱", "高", "Boss 与玩家交火高发区")),
                        List.of(),
                        List.of()
                ));

        mockMvc.perform(get("/api/tarkov/maps/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nameEn").value("Customs"))
                .andExpect(jsonPath("$.data.extracts[0].name").value("Crossroads"))
                .andExpect(jsonPath("$.data.lootAreas[0].riskLevel").value("高"));
    }

    @Test
    void getsHideoutStationDetail() throws Exception {
        given(tarkovCatalogService.getHideoutStationDetail(2L))
                .willReturn(new HideoutStationDetailResponse(
                        2L,
                        "Workbench",
                        "工作台",
                        "用于武器改装和弹药制作。",
                        List.of(new HideoutUpgradeResponse(1L, 1, "螺丝 x2", "1小时", "解锁基础制作"))
                ));

        mockMvc.perform(get("/api/tarkov/hideout/stations/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nameEn").value("Workbench"))
                .andExpect(jsonPath("$.data.upgrades[0].level").value(1));
    }
}
