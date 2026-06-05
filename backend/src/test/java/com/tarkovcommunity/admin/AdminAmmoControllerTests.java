package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminAmmoController;
import com.tarkovcommunity.admin.dto.AdminAmmoResponse;
import com.tarkovcommunity.admin.dto.AdminAmmoUpdateRequest;
import com.tarkovcommunity.admin.service.AdminAmmoService;
import com.tarkovcommunity.common.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAmmoController.class)
class AdminAmmoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminAmmoService adminAmmoService;

    @Test
    void listsAmmo() throws Exception {
        given(adminAmmoService.listAmmo("5.45x39", "ENABLED", "BT", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(ammoResponse())));

        mockMvc.perform(get("/api/admin/ammo")
                        .param("caliber", "5.45x39")
                        .param("status", "ENABLED")
                        .param("keyword", "BT")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].nameEn").value("5.45x39mm BT"))
                .andExpect(jsonPath("$.data.records[0].caliber").value("5.45x39"))
                .andExpect(jsonPath("$.data.records[0].penetration").value(37));
    }

    @Test
    void updatesAmmo() throws Exception {
        AdminAmmoUpdateRequest request = new AdminAmmoUpdateRequest(
                "5.45x39mm BT",
                "5.45x39mm BT弹",
                "5.45x39",
                44,
                37,
                49,
                "Balanced mid-tier rifle ammo.",
                "ENABLED"
        );
        given(adminAmmoService.updateAmmo(eq(1L), any(AdminAmmoUpdateRequest.class)))
                .willReturn(ammoResponse());

        mockMvc.perform(put("/api/admin/ammo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.damage").value(44))
                .andExpect(jsonPath("$.data.armorDamage").value(49))
                .andExpect(jsonPath("$.data.description").value("Balanced mid-tier rifle ammo."));
    }

    @Test
    void rejectsInvalidAmmoUpdate() throws Exception {
        AdminAmmoUpdateRequest request = new AdminAmmoUpdateRequest(
                "",
                "5.45x39mm BT弹",
                "",
                -1,
                37,
                49,
                "说明",
                "UNKNOWN"
        );

        mockMvc.perform(put("/api/admin/ammo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static AdminAmmoResponse ammoResponse() {
        return new AdminAmmoResponse(
                1L,
                "5.45x39mm BT",
                "5.45x39mm BT弹",
                "5.45x39",
                44,
                37,
                49,
                "Balanced mid-tier rifle ammo.",
                "ENABLED"
        );
    }
}
