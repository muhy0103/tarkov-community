package com.tarkovcommunity.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminItemResponse;
import com.tarkovcommunity.admin.dto.AdminItemUpdateRequest;
import com.tarkovcommunity.admin.service.impl.AdminItemServiceImpl;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.TarkovItem;
import com.tarkovcommunity.tarkov.mapper.TarkovItemMapper;
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
class AdminItemServiceImplTests {

    @Mock
    private TarkovItemMapper itemMapper;

    @Test
    void listsItemsWithManagementFields() {
        AdminItemServiceImpl service = new AdminItemServiceImpl(itemMapper);
        Page<TarkovItem> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(item()));
        given(itemMapper.selectPage(any(), any())).willReturn(page);

        PageResponse<AdminItemResponse> response = service.listItems("Key", "ENABLED", true, false, true, "Dorm", 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).singleElement()
                .satisfies(item -> {
                    assertThat(item.nameEn()).isEqualTo("Dorm room 206 key");
                    assertThat(item.itemType()).isEqualTo("Key");
                    assertThat(item.rarity()).isEqualTo("Rare");
                    assertThat(item.gridSize()).isEqualTo("1x1");
                    assertThat(item.questNeeded()).isTrue();
                    assertThat(item.hideoutNeeded()).isFalse();
                    assertThat(item.keepSuggestion()).isTrue();
                    assertThat(item.description()).isEqualTo("Early quest key worth keeping.");
                });
    }

    @Test
    void updatesItemAndNormalizesOptionalText() {
        AdminItemServiceImpl service = new AdminItemServiceImpl(itemMapper);
        given(itemMapper.selectById(1L)).willReturn(item(), updatedItem());

        AdminItemUpdateRequest request = new AdminItemUpdateRequest(
                " Dorm room 206 key ",
                " 206宿舍钥匙 ",
                " Key ",
                " Rare ",
                " 1x1 ",
                true,
                false,
                true,
                " Early quest key worth keeping. ",
                "ENABLED"
        );

        AdminItemResponse response = service.updateItem(1L, request);

        ArgumentCaptor<TarkovItem> itemCaptor = ArgumentCaptor.forClass(TarkovItem.class);
        verify(itemMapper).updateById(itemCaptor.capture());
        TarkovItem savedItem = itemCaptor.getValue();
        assertThat(savedItem.getNameEn()).isEqualTo("Dorm room 206 key");
        assertThat(savedItem.getNameZh()).isEqualTo("206宿舍钥匙");
        assertThat(savedItem.getItemType()).isEqualTo("Key");
        assertThat(savedItem.getRarity()).isEqualTo("Rare");
        assertThat(savedItem.getGridSize()).isEqualTo("1x1");
        assertThat(savedItem.getQuestNeeded()).isTrue();
        assertThat(savedItem.getHideoutNeeded()).isFalse();
        assertThat(savedItem.getKeepSuggestion()).isTrue();
        assertThat(savedItem.getDescription()).isEqualTo("Early quest key worth keeping.");
        assertThat(response.nameEn()).isEqualTo("Dorm room 206 key");
    }

    private static TarkovItem item() {
        TarkovItem item = new TarkovItem();
        item.setId(1L);
        item.setNameEn("Dorm room 206 key");
        item.setNameZh("206宿舍钥匙");
        item.setItemType("Key");
        item.setRarity("Rare");
        item.setGridSize("1x1");
        item.setQuestNeeded(true);
        item.setHideoutNeeded(false);
        item.setKeepSuggestion(true);
        item.setDescription("Early quest key worth keeping.");
        item.setStatus("ENABLED");
        return item;
    }

    private static TarkovItem updatedItem() {
        return item();
    }
}
