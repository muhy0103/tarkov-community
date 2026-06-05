package com.tarkovcommunity.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminQuestPrerequisiteResponse;
import com.tarkovcommunity.admin.dto.AdminQuestPrerequisiteUpdateRequest;
import com.tarkovcommunity.admin.service.impl.AdminQuestPrerequisiteServiceImpl;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.QuestPrerequisite;
import com.tarkovcommunity.tarkov.entity.TarkovQuest;
import com.tarkovcommunity.tarkov.mapper.QuestPrerequisiteMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovQuestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminQuestPrerequisiteServiceImplTests {

    @Mock
    private QuestPrerequisiteMapper questPrerequisiteMapper;

    @Mock
    private TarkovQuestMapper questMapper;

    @Test
    void listsQuestPrerequisitesWithQuestNames() {
        AdminQuestPrerequisiteServiceImpl service =
                new AdminQuestPrerequisiteServiceImpl(questPrerequisiteMapper, questMapper);
        Page<QuestPrerequisite> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(prerequisite()));
        given(questMapper.selectList(any())).willReturn(List.of(debutQuest()));
        given(questPrerequisiteMapper.selectPage(any(), any())).willReturn(page);
        given(questMapper.selectBatchIds(List.of(2L, 1L))).willReturn(List.of(checkingQuest(), debutQuest()));

        PageResponse<AdminQuestPrerequisiteResponse> response =
                service.listQuestPrerequisites(2L, 1L, "Debut", 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).singleElement()
                .satisfies(prerequisite -> {
                    assertThat(prerequisite.questName()).isEqualTo("Checking");
                    assertThat(prerequisite.prerequisiteQuestName()).isEqualTo("Debut");
                    assertThat(prerequisite.questId()).isEqualTo(2L);
                    assertThat(prerequisite.prerequisiteQuestId()).isEqualTo(1L);
                });
    }

    @Test
    void updatesQuestPrerequisite() {
        AdminQuestPrerequisiteServiceImpl service =
                new AdminQuestPrerequisiteServiceImpl(questPrerequisiteMapper, questMapper);
        given(questPrerequisiteMapper.selectById(1L)).willReturn(prerequisite(), updatedPrerequisite());
        given(questMapper.selectById(3L)).willReturn(updatedQuest());
        given(questMapper.selectById(1L)).willReturn(debutQuest());
        given(questMapper.selectBatchIds(List.of(3L, 1L))).willReturn(List.of(updatedQuest(), debutQuest()));

        AdminQuestPrerequisiteResponse response =
                service.updateQuestPrerequisite(1L, new AdminQuestPrerequisiteUpdateRequest(3L, 1L));

        ArgumentCaptor<QuestPrerequisite> prerequisiteCaptor = ArgumentCaptor.forClass(QuestPrerequisite.class);
        verify(questPrerequisiteMapper).updateById(prerequisiteCaptor.capture());
        QuestPrerequisite savedPrerequisite = prerequisiteCaptor.getValue();
        assertThat(savedPrerequisite.getQuestId()).isEqualTo(3L);
        assertThat(savedPrerequisite.getPrerequisiteQuestId()).isEqualTo(1L);
        assertThat(response.questName()).isEqualTo("The Extortionist");
        assertThat(response.prerequisiteQuestName()).isEqualTo("Debut");
    }

    @Test
    void rejectsSelfPrerequisite() {
        AdminQuestPrerequisiteServiceImpl service =
                new AdminQuestPrerequisiteServiceImpl(questPrerequisiteMapper, questMapper);
        given(questPrerequisiteMapper.selectById(1L)).willReturn(prerequisite());

        assertThatThrownBy(() -> service.updateQuestPrerequisite(1L, new AdminQuestPrerequisiteUpdateRequest(2L, 2L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    private static QuestPrerequisite prerequisite() {
        QuestPrerequisite prerequisite = new QuestPrerequisite();
        prerequisite.setId(1L);
        prerequisite.setQuestId(2L);
        prerequisite.setPrerequisiteQuestId(1L);
        return prerequisite;
    }

    private static QuestPrerequisite updatedPrerequisite() {
        QuestPrerequisite prerequisite = new QuestPrerequisite();
        prerequisite.setId(1L);
        prerequisite.setQuestId(3L);
        prerequisite.setPrerequisiteQuestId(1L);
        return prerequisite;
    }

    private static TarkovQuest debutQuest() {
        TarkovQuest quest = new TarkovQuest();
        quest.setId(1L);
        quest.setNameEn("Debut");
        return quest;
    }

    private static TarkovQuest checkingQuest() {
        TarkovQuest quest = new TarkovQuest();
        quest.setId(2L);
        quest.setNameEn("Checking");
        return quest;
    }

    private static TarkovQuest updatedQuest() {
        TarkovQuest quest = new TarkovQuest();
        quest.setId(3L);
        quest.setNameEn("The Extortionist");
        return quest;
    }
}
