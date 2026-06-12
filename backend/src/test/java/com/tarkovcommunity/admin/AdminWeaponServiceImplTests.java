package com.tarkovcommunity.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminWeaponResponse;
import com.tarkovcommunity.admin.dto.AdminWeaponUpdateRequest;
import com.tarkovcommunity.admin.service.impl.AdminWeaponServiceImpl;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.TarkovWeapon;
import com.tarkovcommunity.tarkov.mapper.TarkovWeaponMapper;
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
class AdminWeaponServiceImplTests {

    private static final String IMAGE_URL = "https://assets.tarkov.dev/ak-74n.webp";

    @Mock
    private TarkovWeaponMapper weaponMapper;

    @Test
    void listsWeaponsWithManagementFields() {
        AdminWeaponServiceImpl service = new AdminWeaponServiceImpl(weaponMapper);
        Page<TarkovWeapon> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(weapon()));
        given(weaponMapper.selectPage(any(), any())).willReturn(page);

        PageResponse<AdminWeaponResponse> response = service.listWeapons("Assault rifle", "5.45x39", "ENABLED", "AK", 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).singleElement()
                .satisfies(weapon -> {
                    assertThat(weapon.nameEn()).isEqualTo("AK-74N");
                    assertThat(weapon.nameZh()).isEqualTo("AK-74N突击步枪");
                    assertThat(weapon.weaponType()).isEqualTo("Assault rifle");
                    assertThat(weapon.caliber()).isEqualTo("5.45x39");
                    assertThat(weapon.description()).isEqualTo("Classic early wipe rifle.");
                    assertThat(weapon.imageUrl()).isEqualTo(IMAGE_URL);
                });
    }

    @Test
    void updatesWeaponAndNormalizesOptionalText() {
        AdminWeaponServiceImpl service = new AdminWeaponServiceImpl(weaponMapper);
        given(weaponMapper.selectById(1L)).willReturn(weapon(), updatedWeapon());

        AdminWeaponUpdateRequest request = new AdminWeaponUpdateRequest(
                " AK-74N ",
                " AK-74N突击步枪 ",
                " Assault rifle ",
                " 5.45x39 ",
                " Classic early wipe rifle. ",
                " " + IMAGE_URL + " ",
                "ENABLED"
        );

        AdminWeaponResponse response = service.updateWeapon(1L, request);

        ArgumentCaptor<TarkovWeapon> weaponCaptor = ArgumentCaptor.forClass(TarkovWeapon.class);
        verify(weaponMapper).updateById(weaponCaptor.capture());
        TarkovWeapon savedWeapon = weaponCaptor.getValue();
        assertThat(savedWeapon.getNameEn()).isEqualTo("AK-74N");
        assertThat(savedWeapon.getNameZh()).isEqualTo("AK-74N突击步枪");
        assertThat(savedWeapon.getWeaponType()).isEqualTo("Assault rifle");
        assertThat(savedWeapon.getCaliber()).isEqualTo("5.45x39");
        assertThat(savedWeapon.getDescription()).isEqualTo("Classic early wipe rifle.");
        assertThat(savedWeapon.getImageUrl()).isEqualTo(IMAGE_URL);
        assertThat(response.imageUrl()).isEqualTo(IMAGE_URL);
        assertThat(response.nameEn()).isEqualTo("AK-74N");
    }

    private static TarkovWeapon weapon() {
        TarkovWeapon weapon = new TarkovWeapon();
        weapon.setId(1L);
        weapon.setNameEn("AK-74N");
        weapon.setNameZh("AK-74N突击步枪");
        weapon.setWeaponType("Assault rifle");
        weapon.setCaliber("5.45x39");
        weapon.setDescription("Classic early wipe rifle.");
        weapon.setImageUrl(IMAGE_URL);
        weapon.setStatus("ENABLED");
        return weapon;
    }

    private static TarkovWeapon updatedWeapon() {
        return weapon();
    }
}
