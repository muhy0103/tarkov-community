package com.tarkovcommunity.forum;

import com.tarkovcommunity.forum.dto.PostCatalogRelationRequest;
import com.tarkovcommunity.forum.entity.PostCatalogRelation;
import com.tarkovcommunity.forum.mapper.PostCatalogRelationMapper;
import com.tarkovcommunity.forum.service.impl.PostCatalogRelationServiceImpl;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
import com.tarkovcommunity.tarkov.mapper.BossMapper;
import com.tarkovcommunity.tarkov.mapper.HideoutStationMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovAmmoMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovItemMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovMapMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovQuestMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovTraderMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovWeaponMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostCatalogRelationServiceImplTests {

    @Mock
    private PostCatalogRelationMapper relationMapper;

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
    private BossMapper bossMapper;

    @Mock
    private HideoutStationMapper hideoutStationMapper;

    @Test
    void replacesRelationsWithDedupedRows() {
        PostCatalogRelationServiceImpl service = service();
        given(mapMapper.selectById(1L)).willReturn(enabledMap());

        service.replaceRelations(10L, List.of(
                new PostCatalogRelationRequest("MAP", 1L, "main map"),
                new PostCatalogRelationRequest("MAP", 1L, "duplicate")
        ));

        verify(relationMapper).delete(any());
        ArgumentCaptor<PostCatalogRelation> relationCaptor = ArgumentCaptor.forClass(PostCatalogRelation.class);
        verify(relationMapper).insert(relationCaptor.capture());
        PostCatalogRelation savedRelation = relationCaptor.getValue();
        assertThat(savedRelation.getPostId()).isEqualTo(10L);
        assertThat(savedRelation.getCatalogType()).isEqualTo("MAP");
        assertThat(savedRelation.getCatalogId()).isEqualTo(1L);
        assertThat(savedRelation.getRelationNote()).isEqualTo("main map");
    }

    @Test
    void rejectsMoreThanSixRelations() {
        PostCatalogRelationServiceImpl service = service();
        List<PostCatalogRelationRequest> requests = LongStream.rangeClosed(1, 7)
                .mapToObj(id -> new PostCatalogRelationRequest("MAP", id, null))
                .toList();

        assertThatThrownBy(() -> service.replaceRelations(10L, requests))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("6");
    }

    private PostCatalogRelationServiceImpl service() {
        return new PostCatalogRelationServiceImpl(
                relationMapper,
                mapMapper,
                traderMapper,
                questMapper,
                itemMapper,
                weaponMapper,
                ammoMapper,
                bossMapper,
                hideoutStationMapper
        );
    }

    private static TarkovMap enabledMap() {
        TarkovMap map = new TarkovMap();
        map.setId(1L);
        map.setNameEn("Customs");
        map.setNameZh("Customs");
        map.setStatus("ENABLED");
        return map;
    }
}
