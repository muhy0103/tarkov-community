package com.tarkovcommunity.tarkov;

import com.tarkovcommunity.tarkov.dto.BossResponse;
import com.tarkovcommunity.tarkov.dto.TraderResponse;
import com.tarkovcommunity.tarkov.entity.Boss;
import com.tarkovcommunity.tarkov.entity.TarkovTrader;
import com.tarkovcommunity.tarkov.mapper.BossMapper;
import com.tarkovcommunity.tarkov.mapper.HideoutStationMapper;
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

    private TarkovCatalogServiceImpl service() {
        return new TarkovCatalogServiceImpl(
                mapMapper,
                traderMapper,
                questMapper,
                itemMapper,
                weaponMapper,
                ammoMapper,
                hideoutStationMapper,
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
}
