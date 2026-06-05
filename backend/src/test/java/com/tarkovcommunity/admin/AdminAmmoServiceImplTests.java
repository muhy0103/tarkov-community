package com.tarkovcommunity.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminAmmoResponse;
import com.tarkovcommunity.admin.dto.AdminAmmoUpdateRequest;
import com.tarkovcommunity.admin.service.impl.AdminAmmoServiceImpl;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.TarkovAmmo;
import com.tarkovcommunity.tarkov.mapper.TarkovAmmoMapper;
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
class AdminAmmoServiceImplTests {

    @Mock
    private TarkovAmmoMapper ammoMapper;

    @Test
    void listsAmmoWithManagementFields() {
        AdminAmmoServiceImpl service = new AdminAmmoServiceImpl(ammoMapper);
        Page<TarkovAmmo> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(ammo()));
        given(ammoMapper.selectPage(any(), any())).willReturn(page);

        PageResponse<AdminAmmoResponse> response = service.listAmmo("5.45x39", "ENABLED", "BT", 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).singleElement()
                .satisfies(ammo -> {
                    assertThat(ammo.nameEn()).isEqualTo("5.45x39mm BT");
                    assertThat(ammo.caliber()).isEqualTo("5.45x39");
                    assertThat(ammo.damage()).isEqualTo(44);
                    assertThat(ammo.penetration()).isEqualTo(37);
                    assertThat(ammo.armorDamage()).isEqualTo(49);
                    assertThat(ammo.description()).isEqualTo("Balanced mid-tier rifle ammo.");
                });
    }

    @Test
    void updatesAmmoAndNormalizesOptionalText() {
        AdminAmmoServiceImpl service = new AdminAmmoServiceImpl(ammoMapper);
        given(ammoMapper.selectById(1L)).willReturn(ammo(), updatedAmmo());

        AdminAmmoUpdateRequest request = new AdminAmmoUpdateRequest(
                " 5.45x39mm BT ",
                " 5.45x39mm BT弹 ",
                " 5.45x39 ",
                44,
                37,
                49,
                " Balanced mid-tier rifle ammo. ",
                "ENABLED"
        );

        AdminAmmoResponse response = service.updateAmmo(1L, request);

        ArgumentCaptor<TarkovAmmo> ammoCaptor = ArgumentCaptor.forClass(TarkovAmmo.class);
        verify(ammoMapper).updateById(ammoCaptor.capture());
        TarkovAmmo savedAmmo = ammoCaptor.getValue();
        assertThat(savedAmmo.getNameEn()).isEqualTo("5.45x39mm BT");
        assertThat(savedAmmo.getNameZh()).isEqualTo("5.45x39mm BT弹");
        assertThat(savedAmmo.getCaliber()).isEqualTo("5.45x39");
        assertThat(savedAmmo.getDamage()).isEqualTo(44);
        assertThat(savedAmmo.getPenetration()).isEqualTo(37);
        assertThat(savedAmmo.getArmorDamage()).isEqualTo(49);
        assertThat(savedAmmo.getDescription()).isEqualTo("Balanced mid-tier rifle ammo.");
        assertThat(response.nameEn()).isEqualTo("5.45x39mm BT");
    }

    private static TarkovAmmo ammo() {
        TarkovAmmo ammo = new TarkovAmmo();
        ammo.setId(1L);
        ammo.setNameEn("5.45x39mm BT");
        ammo.setNameZh("5.45x39mm BT弹");
        ammo.setCaliber("5.45x39");
        ammo.setDamage(44);
        ammo.setPenetration(37);
        ammo.setArmorDamage(49);
        ammo.setDescription("Balanced mid-tier rifle ammo.");
        ammo.setStatus("ENABLED");
        return ammo;
    }

    private static TarkovAmmo updatedAmmo() {
        return ammo();
    }
}
