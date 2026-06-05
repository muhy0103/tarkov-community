package com.tarkovcommunity.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminTagResponse;
import com.tarkovcommunity.admin.dto.AdminTagUpdateRequest;
import com.tarkovcommunity.admin.service.impl.AdminTagServiceImpl;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.meta.entity.Tag;
import com.tarkovcommunity.meta.mapper.TagMapper;
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
class AdminTagServiceImplTests {

    @Mock
    private TagMapper tagMapper;

    @Test
    void listsTags() {
        AdminTagServiceImpl service = new AdminTagServiceImpl(tagMapper);
        Page<Tag> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(tag()));
        given(tagMapper.selectPage(any(), any())).willReturn(page);

        PageResponse<AdminTagResponse> response =
                service.listTags("SYSTEM", "ENABLED", "任务", 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).singleElement()
                .satisfies(tag -> {
                    assertThat(tag.name()).isEqualTo("任务攻略");
                    assertThat(tag.type()).isEqualTo("SYSTEM");
                    assertThat(tag.color()).isEqualTo("#2563EB");
                    assertThat(tag.status()).isEqualTo("ENABLED");
                });
    }

    @Test
    void updatesTagAndNormalizesText() {
        AdminTagServiceImpl service = new AdminTagServiceImpl(tagMapper);
        given(tagMapper.selectById(1L)).willReturn(tag(), updatedTag());

        AdminTagResponse response = service.updateTag(1L, new AdminTagUpdateRequest(
                " 任务攻略 ",
                " SYSTEM ",
                " #2563EB ",
                "ENABLED"
        ));

        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagMapper).updateById(tagCaptor.capture());
        Tag savedTag = tagCaptor.getValue();
        assertThat(savedTag.getName()).isEqualTo("任务攻略");
        assertThat(savedTag.getType()).isEqualTo("SYSTEM");
        assertThat(savedTag.getColor()).isEqualTo("#2563EB");
        assertThat(savedTag.getStatus()).isEqualTo("ENABLED");
        assertThat(response.name()).isEqualTo("任务攻略");
    }

    private static Tag tag() {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("任务攻略");
        tag.setType("SYSTEM");
        tag.setColor("#2563EB");
        tag.setStatus("ENABLED");
        return tag;
    }

    private static Tag updatedTag() {
        return tag();
    }
}
