package com.tarkovcommunity.tarkov;

import com.tarkovcommunity.tarkov.controller.TarkovCatalogController;
import com.tarkovcommunity.tarkov.dto.TarkovMapResponse;
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
                .willReturn(List.of(new TraderResponse(1L, "Prapor", "普拉波", "默认解锁")));

        mockMvc.perform(get("/api/tarkov/traders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].nameZh").value("普拉波"));
    }
}
