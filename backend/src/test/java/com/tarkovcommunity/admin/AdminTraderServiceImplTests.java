package com.tarkovcommunity.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminTraderResponse;
import com.tarkovcommunity.admin.dto.AdminTraderUpdateRequest;
import com.tarkovcommunity.admin.service.impl.AdminTraderServiceImpl;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.TarkovTrader;
import com.tarkovcommunity.tarkov.mapper.TarkovTraderMapper;
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
class AdminTraderServiceImplTests {

    @Mock
    private TarkovTraderMapper traderMapper;

    @Test
    void listsTradersWithoutChineseDisplayName() {
        AdminTraderServiceImpl service = new AdminTraderServiceImpl(traderMapper);
        Page<TarkovTrader> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(trader()));
        given(traderMapper.selectPage(any(), any())).willReturn(page);

        PageResponse<AdminTraderResponse> response = service.listTraders("ENABLED", "Prapor", 1, 10);

        assertThat(response.records()).singleElement()
                .satisfies(trader -> {
                    assertThat(trader.nameEn()).isEqualTo("Prapor");
                    assertThat(trader.nameZh()).isNull();
                });
    }

    @Test
    void updatesTraderUsingEnglishNameForStoredCompatibilityName() {
        AdminTraderServiceImpl service = new AdminTraderServiceImpl(traderMapper);
        given(traderMapper.selectById(1L)).willReturn(trader(), updatedTrader());

        AdminTraderUpdateRequest request = new AdminTraderUpdateRequest(
                " Prapor ",
                null,
                " Early quests and basic weapons. ",
                "Available from the start",
                "https://example.com/prapor.png",
                "ENABLED"
        );

        AdminTraderResponse response = service.updateTrader(1L, request);

        ArgumentCaptor<TarkovTrader> traderCaptor = ArgumentCaptor.forClass(TarkovTrader.class);
        verify(traderMapper).updateById(traderCaptor.capture());
        TarkovTrader savedTrader = traderCaptor.getValue();
        assertThat(savedTrader.getNameEn()).isEqualTo("Prapor");
        assertThat(savedTrader.getNameZh()).isEqualTo("Prapor");
        assertThat(savedTrader.getDescription()).isEqualTo("Early quests and basic weapons.");
        assertThat(response.nameZh()).isNull();
    }

    private static TarkovTrader trader() {
        TarkovTrader trader = new TarkovTrader();
        trader.setId(1L);
        trader.setNameEn("Prapor");
        trader.setNameZh("普拉波");
        trader.setDescription("Early quests and basic weapons.");
        trader.setUnlockCondition("Available from the start");
        trader.setAvatar("https://example.com/prapor.png");
        trader.setStatus("ENABLED");
        return trader;
    }

    private static TarkovTrader updatedTrader() {
        TarkovTrader trader = trader();
        trader.setNameZh("Prapor");
        return trader;
    }
}
