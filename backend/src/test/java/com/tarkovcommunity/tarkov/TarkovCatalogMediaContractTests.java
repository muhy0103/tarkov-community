package com.tarkovcommunity.tarkov;

import com.tarkovcommunity.tarkov.dto.AmmoDetailResponse;
import com.tarkovcommunity.tarkov.dto.AmmoResponse;
import com.tarkovcommunity.tarkov.dto.BossDetailResponse;
import com.tarkovcommunity.tarkov.dto.BossResponse;
import com.tarkovcommunity.tarkov.dto.TarkovMapDetailResponse;
import com.tarkovcommunity.tarkov.dto.TarkovMapResponse;
import com.tarkovcommunity.tarkov.dto.TraderDetailResponse;
import com.tarkovcommunity.tarkov.dto.TraderResponse;
import com.tarkovcommunity.tarkov.dto.WeaponDetailResponse;
import com.tarkovcommunity.tarkov.dto.WeaponResponse;
import com.tarkovcommunity.tarkov.entity.Boss;
import com.tarkovcommunity.tarkov.entity.TarkovAmmo;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
import com.tarkovcommunity.tarkov.entity.TarkovTrader;
import com.tarkovcommunity.tarkov.entity.TarkovWeapon;
import com.tarkovcommunity.tarkov.mapper.BossMapper;
import com.tarkovcommunity.tarkov.mapper.HideoutStationMapper;
import com.tarkovcommunity.tarkov.mapper.HideoutUpgradeMapper;
import com.tarkovcommunity.tarkov.mapper.MapExtractMapper;
import com.tarkovcommunity.tarkov.mapper.MapLootAreaMapper;
import com.tarkovcommunity.tarkov.mapper.QuestPrerequisiteMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovAmmoMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovItemMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovMapMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovQuestMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovTraderMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovWeaponMapper;
import com.tarkovcommunity.tarkov.service.impl.TarkovCatalogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TarkovCatalogMediaContractTests {

    private static final String MAP_IMAGE = "https://assets.tarkov.dev/customs.webp";
    private static final String TRADER_AVATAR = "https://assets.tarkov.dev/prapor.webp";
    private static final String WEAPON_IMAGE = "https://assets.tarkov.dev/ak-74n.webp";
    private static final String AMMO_IMAGE = "https://assets.tarkov.dev/54527a984bdc2d4e668b4567-512.webp";
    private static final String BOSS_IMAGE = "https://assets.tarkov.dev/reshala.webp";

    @Mock
    private TarkovMapMapper mapMapper;

    @Mock
    private TarkovTraderMapper traderMapper;

    @Mock
    private TarkovQuestMapper questMapper;

    @Mock
    private TarkovItemMapper itemMapper;

    @Mock
    private TarkovWeaponMapper weaponMapper;

    @Mock
    private TarkovAmmoMapper ammoMapper;

    @Mock
    private HideoutStationMapper hideoutStationMapper;

    @Mock
    private HideoutUpgradeMapper hideoutUpgradeMapper;

    @Mock
    private MapExtractMapper mapExtractMapper;

    @Mock
    private MapLootAreaMapper mapLootAreaMapper;

    @Mock
    private QuestPrerequisiteMapper questPrerequisiteMapper;

    @Mock
    private BossMapper bossMapper;

    @Test
    void exposesMediaFieldsInCatalogLists() {
        given(mapMapper.selectList(any())).willReturn(List.of(map()));
        given(traderMapper.selectList(any())).willReturn(List.of(trader()));
        given(weaponMapper.selectList(any())).willReturn(List.of(weapon()));
        given(ammoMapper.selectList(any())).willReturn(List.of(ammo()));
        given(bossMapper.selectList(any())).willReturn(List.of(boss()));
        TarkovCatalogServiceImpl service = service();

        List<TarkovMapResponse> maps = service.listMaps();
        List<TraderResponse> traders = service.listTraders();
        List<WeaponResponse> weapons = service.listWeapons();
        List<AmmoResponse> ammo = service.listAmmo();
        List<BossResponse> bosses = service.listBosses();

        assertThat(maps).singleElement().satisfies(map -> assertThat(map.imageUrl()).isEqualTo(MAP_IMAGE));
        assertThat(traders).singleElement().satisfies(trader -> assertThat(trader.avatar()).isEqualTo(TRADER_AVATAR));
        assertThat(weapons).singleElement().satisfies(weapon -> assertThat(weapon.imageUrl()).isEqualTo(WEAPON_IMAGE));
        assertThat(ammo).singleElement().satisfies(round -> assertThat(round.imageUrl()).isEqualTo(AMMO_IMAGE));
        assertThat(bosses).singleElement().satisfies(boss -> assertThat(boss.imageUrl()).isEqualTo(BOSS_IMAGE));
    }

    @Test
    void exposesMediaFieldsInCatalogDetails() {
        given(mapMapper.selectOne(any())).willReturn(map());
        given(mapExtractMapper.selectList(any())).willReturn(List.of());
        given(mapLootAreaMapper.selectList(any())).willReturn(List.of());
        given(bossMapper.selectList(any())).willReturn(List.of());
        given(questMapper.selectList(any())).willReturn(List.of());
        given(traderMapper.selectOne(any())).willReturn(trader());
        given(weaponMapper.selectOne(any())).willReturn(weapon());
        given(ammoMapper.selectList(any())).willReturn(List.of(ammo()));
        given(ammoMapper.selectOne(any())).willReturn(ammo());
        given(weaponMapper.selectList(any())).willReturn(List.of(weapon()));
        given(bossMapper.selectOne(any())).willReturn(boss());
        TarkovCatalogServiceImpl service = service();

        TarkovMapDetailResponse map = service.getMapDetail(1L);
        TraderDetailResponse trader = service.getTraderDetail(1L);
        WeaponDetailResponse weapon = service.getWeaponDetail(1L);
        AmmoDetailResponse ammo = service.getAmmoDetail(1L);
        BossDetailResponse boss = service.getBossDetail(1L);

        assertThat(map.imageUrl()).isEqualTo(MAP_IMAGE);
        assertThat(trader.avatar()).isEqualTo(TRADER_AVATAR);
        assertThat(weapon.imageUrl()).isEqualTo(WEAPON_IMAGE);
        assertThat(weapon.compatibleAmmo()).singleElement()
                .satisfies(round -> assertThat(round.imageUrl()).isEqualTo(AMMO_IMAGE));
        assertThat(ammo.imageUrl()).isEqualTo(AMMO_IMAGE);
        assertThat(ammo.compatibleWeapons()).singleElement()
                .satisfies(compatibleWeapon -> assertThat(compatibleWeapon.imageUrl()).isEqualTo(WEAPON_IMAGE));
        assertThat(boss.imageUrl()).isEqualTo(BOSS_IMAGE);
        assertThat(boss.map().imageUrl()).isEqualTo(MAP_IMAGE);
    }

    private TarkovCatalogServiceImpl service() {
        return new TarkovCatalogServiceImpl(
                mapMapper,
                traderMapper,
                questMapper,
                itemMapper,
                weaponMapper,
                ammoMapper,
                hideoutStationMapper,
                hideoutUpgradeMapper,
                mapExtractMapper,
                mapLootAreaMapper,
                questPrerequisiteMapper,
                bossMapper
        );
    }

    private static TarkovMap map() {
        TarkovMap map = new TarkovMap();
        map.setId(1L);
        map.setNameEn("Customs");
        map.setNameZh("Customs");
        map.setDifficulty("Medium");
        map.setDescription("Transit-heavy starter map.");
        map.setRecommendedLevel("1+");
        map.setImageUrl(MAP_IMAGE);
        map.setStatus("ENABLED");
        return map;
    }

    private static TarkovTrader trader() {
        TarkovTrader trader = new TarkovTrader();
        trader.setId(1L);
        trader.setNameEn("Prapor");
        trader.setNameZh("Prapor");
        trader.setDescription("Early-game equipment trader.");
        trader.setUnlockCondition("Available from the start");
        trader.setAvatar(TRADER_AVATAR);
        trader.setStatus("ENABLED");
        return trader;
    }

    private static TarkovWeapon weapon() {
        TarkovWeapon weapon = new TarkovWeapon();
        weapon.setId(1L);
        weapon.setNameEn("AK-74N");
        weapon.setNameZh("AK-74N");
        weapon.setWeaponType("Assault rifle");
        weapon.setCaliber("5.45x39mm");
        weapon.setDescription("5.45 assault rifle.");
        weapon.setImageUrl(WEAPON_IMAGE);
        weapon.setStatus("ENABLED");
        return weapon;
    }

    private static TarkovAmmo ammo() {
        TarkovAmmo ammo = new TarkovAmmo();
        ammo.setId(1L);
        ammo.setNameEn("5.45x39mm PS");
        ammo.setNameZh("5.45x39mm PS");
        ammo.setCaliber("5.45x39mm");
        ammo.setDamage(48);
        ammo.setPenetration(31);
        ammo.setArmorDamage(32);
        ammo.setDescription("General-purpose 5.45 round.");
        ammo.setImageUrl(AMMO_IMAGE);
        ammo.setStatus("ENABLED");
        return ammo;
    }

    private static Boss boss() {
        Boss boss = new Boss();
        boss.setId(1L);
        boss.setNameEn("Reshala");
        boss.setNameZh("Reshala");
        boss.setMapId(1L);
        boss.setDescription("Customs boss.");
        boss.setEquipmentSummary("Guards and mid-tier rifles.");
        boss.setImageUrl(BOSS_IMAGE);
        boss.setStatus("ENABLED");
        return boss;
    }
}
