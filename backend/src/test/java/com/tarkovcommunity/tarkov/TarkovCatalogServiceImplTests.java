package com.tarkovcommunity.tarkov;

import com.tarkovcommunity.tarkov.dto.BossResponse;
import com.tarkovcommunity.tarkov.dto.TarkovMapDetailResponse;
import com.tarkovcommunity.tarkov.dto.TraderResponse;
import com.tarkovcommunity.tarkov.dto.WeaponDetailResponse;
import com.tarkovcommunity.tarkov.entity.Boss;
import com.tarkovcommunity.tarkov.entity.MapExtract;
import com.tarkovcommunity.tarkov.entity.MapLootArea;
import com.tarkovcommunity.tarkov.entity.TarkovAmmo;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
import com.tarkovcommunity.tarkov.entity.TarkovQuest;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TarkovCatalogServiceImplTests {

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
    void listsTradersWithEnglishNamesOnly() {
        given(traderMapper.selectList(any())).willReturn(List.of(trader()));
        TarkovCatalogServiceImpl service = service();

        List<TraderResponse> response = service.listTraders();

        assertThat(response).singleElement()
                .satisfies(trader -> {
                    assertThat(trader.nameEn()).isEqualTo("Prapor");
                    assertThat(trader.nameZh()).isNull();
                });
    }

    @Test
    void listsBossesWithEnglishNamesOnly() {
        given(bossMapper.selectList(any())).willReturn(List.of(boss()));
        TarkovCatalogServiceImpl service = service();

        List<BossResponse> response = service.listBosses();

        assertThat(response).singleElement()
                .satisfies(boss -> {
                    assertThat(boss.nameEn()).isEqualTo("Reshala");
                    assertThat(boss.nameZh()).isNull();
                });
    }

    @Test
    void getsMapDetailWithExtractsLootAreasBossesAndQuests() {
        given(mapMapper.selectOne(any())).willReturn(map());
        given(mapExtractMapper.selectList(any())).willReturn(List.of(extract()));
        given(mapLootAreaMapper.selectList(any())).willReturn(List.of(lootArea()));
        given(bossMapper.selectList(any())).willReturn(List.of(boss()));
        given(questMapper.selectList(any())).willReturn(List.of(quest()));
        TarkovCatalogServiceImpl service = service();

        TarkovMapDetailResponse response = service.getMapDetail(1L);

        assertThat(response.nameZh()).isEqualTo("海关");
        assertThat(response.description()).isEqualTo("任务密集、撤离选择多。");
        assertThat(response.extracts()).singleElement()
                .satisfies(extract -> assertThat(extract.name()).isEqualTo("Crossroads"));
        assertThat(response.lootAreas()).singleElement()
                .satisfies(area -> assertThat(area.riskLevel()).isEqualTo("高"));
        assertThat(response.bosses()).singleElement()
                .satisfies(boss -> {
                    assertThat(boss.nameEn()).isEqualTo("Reshala");
                    assertThat(boss.nameZh()).isNull();
                });
        assertThat(response.quests()).singleElement()
                .satisfies(quest -> assertThat(quest.nameZh()).isEqualTo("交货"));
    }

    @Test
    void getsWeaponDetailWithCompatibleAmmo() {
        given(weaponMapper.selectOne(any())).willReturn(weapon());
        given(ammoMapper.selectList(any())).willReturn(List.of(ammo()));
        TarkovCatalogServiceImpl service = service();

        WeaponDetailResponse response = service.getWeaponDetail(1L);

        assertThat(response.nameZh()).isEqualTo("AK-74N");
        assertThat(response.compatibleAmmo()).singleElement()
                .satisfies(ammo -> assertThat(ammo.nameZh()).isEqualTo("5.45x39mm PS"));
    }

    @Test
    void rejectsMissingMapDetail() {
        given(mapMapper.selectOne(any())).willReturn(null);
        TarkovCatalogServiceImpl service = service();

        assertThatThrownBy(() -> service.getMapDetail(404L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
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

    private static TarkovTrader trader() {
        TarkovTrader trader = new TarkovTrader();
        trader.setId(1L);
        trader.setNameEn("Prapor");
        trader.setNameZh("普拉波");
        trader.setUnlockCondition("默认解锁");
        trader.setStatus("ENABLED");
        return trader;
    }

    private static Boss boss() {
        Boss boss = new Boss();
        boss.setId(1L);
        boss.setNameEn("Reshala");
        boss.setNameZh("雷舍拉");
        boss.setMapId(1L);
        boss.setStatus("ENABLED");
        return boss;
    }

    private static TarkovMap map() {
        TarkovMap map = new TarkovMap();
        map.setId(1L);
        map.setNameEn("Customs");
        map.setNameZh("海关");
        map.setDifficulty("中等");
        map.setDescription("任务密集、撤离选择多。");
        map.setRecommendedLevel("1+");
        map.setStatus("ENABLED");
        return map;
    }

    private static MapExtract extract() {
        MapExtract extract = new MapExtract();
        extract.setId(1L);
        extract.setMapId(1L);
        extract.setName("Crossroads");
        extract.setFactionLimit("SCAV/PMC");
        extract.setConditionText("默认开启");
        extract.setDescription("西侧撤离点");
        extract.setStatus("ENABLED");
        return extract;
    }

    private static MapLootArea lootArea() {
        MapLootArea area = new MapLootArea();
        area.setId(1L);
        area.setMapId(1L);
        area.setName("宿舍楼");
        area.setLootType("钥匙房/保险箱");
        area.setRiskLevel("高");
        area.setDescription("Boss 与玩家交火高发区");
        return area;
    }

    private static TarkovQuest quest() {
        TarkovQuest quest = new TarkovQuest();
        quest.setId(1L);
        quest.setTraderId(1L);
        quest.setNameEn("Checking");
        quest.setNameZh("交货");
        quest.setQuestType("探索");
        quest.setMapId(1L);
        quest.setStatus("ENABLED");
        return quest;
    }

    private static TarkovWeapon weapon() {
        TarkovWeapon weapon = new TarkovWeapon();
        weapon.setId(1L);
        weapon.setNameEn("AK-74N");
        weapon.setNameZh("AK-74N");
        weapon.setWeaponType("Assault rifle");
        weapon.setCaliber("5.45x39mm");
        weapon.setDescription("稳定的中期步枪。");
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
        ammo.setDescription("入门可用弹。");
        ammo.setStatus("ENABLED");
        return ammo;
    }
}
