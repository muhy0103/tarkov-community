package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminWeaponController;
import com.tarkovcommunity.admin.dto.AdminWeaponResponse;
import com.tarkovcommunity.admin.dto.AdminWeaponUpdateRequest;
import com.tarkovcommunity.admin.service.AdminWeaponService;
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

@WebMvcTest(AdminWeaponController.class)
class AdminWeaponControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminWeaponService adminWeaponService;

    @Test
    void listsWeapons() throws Exception {
        given(adminWeaponService.listWeapons("Assault rifle", "5.45x39", "ENABLED", "AK", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(weaponResponse())));

        mockMvc.perform(get("/api/admin/weapons")
                        .param("weaponType", "Assault rifle")
                        .param("caliber", "5.45x39")
                        .param("status", "ENABLED")
                        .param("keyword", "AK")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].nameEn").value("AK-74N"))
                .andExpect(jsonPath("$.data.records[0].weaponType").value("Assault rifle"))
                .andExpect(jsonPath("$.data.records[0].caliber").value("5.45x39"));
    }

    @Test
    void updatesWeapon() throws Exception {
        AdminWeaponUpdateRequest request = new AdminWeaponUpdateRequest(
                "AK-74N",
                "AK-74N突击步枪",
                "Assault rifle",
                "5.45x39",
                "Classic early wipe rifle.",
                "ENABLED"
        );
        given(adminWeaponService.updateWeapon(eq(1L), any(AdminWeaponUpdateRequest.class)))
                .willReturn(weaponResponse());

        mockMvc.perform(put("/api/admin/weapons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.description").value("Classic early wipe rifle."))
                .andExpect(jsonPath("$.data.status").value("ENABLED"));
    }

    @Test
    void rejectsInvalidWeaponUpdate() throws Exception {
        AdminWeaponUpdateRequest request = new AdminWeaponUpdateRequest(
                "",
                "AK-74N突击步枪",
                "Assault rifle",
                "5.45x39",
                "说明",
                "UNKNOWN"
        );

        mockMvc.perform(put("/api/admin/weapons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static AdminWeaponResponse weaponResponse() {
        return new AdminWeaponResponse(
                1L,
                "AK-74N",
                "AK-74N突击步枪",
                "Assault rifle",
                "5.45x39",
                "Classic early wipe rifle.",
                "ENABLED"
        );
    }
}
