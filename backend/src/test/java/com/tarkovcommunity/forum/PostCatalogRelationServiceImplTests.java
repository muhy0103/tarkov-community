package com.tarkovcommunity.forum;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.forum.dto.PostCatalogRelationRequest;
import com.tarkovcommunity.forum.dto.RelatedCatalogResponse;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.entity.PostCatalogRelation;
import com.tarkovcommunity.forum.mapper.PostCatalogRelationMapper;
import com.tarkovcommunity.forum.service.impl.PostCatalogRelationServiceImpl;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
import com.tarkovcommunity.tarkov.entity.TarkovWeapon;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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

    @Test
    void rejectsMissingCatalogRecord() {
        PostCatalogRelationServiceImpl service = service();
        given(weaponMapper.selectById(404L)).willReturn(null);

        assertThatThrownBy(() -> service.replaceRelations(
                10L,
                List.of(new PostCatalogRelationRequest("WEAPON", 404L, null))
        ))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                .hasMessageContaining("关联资料不存在或已停用");

        verify(relationMapper, never()).delete(any());
        verify(relationMapper, never()).insert(any(PostCatalogRelation.class));
    }

    @Test
    void hydratesRelatedCatalogResponsesByPostId() {
        PostCatalogRelationServiceImpl service = service();
        PostCatalogRelation relation = new PostCatalogRelation();
        relation.setId(1L);
        relation.setPostId(10L);
        relation.setCatalogType("WEAPON");
        relation.setCatalogId(1L);
        relation.setRelationNote("主武器");
        given(relationMapper.selectList(any())).willReturn(List.of(relation));
        given(weaponMapper.selectBatchIds(List.of(1L))).willReturn(List.of(enabledWeapon()));

        Map<Long, List<RelatedCatalogResponse>> responses = service.findRelationsByPostIds(List.of(10L));

        assertThat(responses).containsOnlyKeys(10L);
        assertThat(responses.get(10L)).hasSize(1);
        RelatedCatalogResponse response = responses.get(10L).get(0);
        assertThat(response.catalogType()).isEqualTo("WEAPON");
        assertThat(response.catalogId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("AK-74N");
        assertThat(response.subtitle()).isEqualTo("Assault rifle · 5.45x39");
        assertThat(response.imageUrl()).isEqualTo("https://assets.example/ak-74n.png");
        assertThat(response.routeKind()).isEqualTo("weapons");
        assertThat(response.relationNote()).isEqualTo("主武器");
    }

    @Test
    void hydratesDuplicateCatalogItemsWithOneBatchLookup() {
        PostCatalogRelationServiceImpl service = service();
        PostCatalogRelation firstRelation = relation(1L, 10L, "WEAPON", 1L, "first");
        PostCatalogRelation secondRelation = relation(2L, 11L, "WEAPON", 1L, "second");
        given(relationMapper.selectList(any())).willReturn(List.of(firstRelation, secondRelation));
        given(weaponMapper.selectBatchIds(List.of(1L))).willReturn(List.of(enabledWeapon()));

        Map<Long, List<RelatedCatalogResponse>> responses = service.findRelationsByPostIds(List.of(10L, 11L));

        assertThat(responses.keySet()).containsExactly(10L, 11L);
        assertThat(responses.get(10L).get(0).catalogId()).isEqualTo(1L);
        assertThat(responses.get(10L).get(0).relationNote()).isEqualTo("first");
        assertThat(responses.get(11L).get(0).catalogId()).isEqualTo(1L);
        assertThat(responses.get(11L).get(0).relationNote()).isEqualTo("second");
        verify(weaponMapper).selectBatchIds(List.of(1L));
        verify(weaponMapper, never()).selectById(1L);
    }

    @Test
    void clampsRelatedPostsPageArguments() {
        PostCatalogRelationServiceImpl service = service();
        Page<Post> selectedPage = new Page<>(1, 50);
        given(weaponMapper.selectById(1L)).willReturn(enabledWeapon());
        given(relationMapper.selectRelatedPostsPage(any(), eq("WEAPON"), eq(1L))).willReturn(selectedPage);

        Page<Post> result = service.selectRelatedPostsPage("weapon", 1L, 0, 100);

        assertThat(result).isSameAs(selectedPage);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Page<Post>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(relationMapper).selectRelatedPostsPage(pageCaptor.capture(), eq("WEAPON"), eq(1L));
        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(1);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(50);
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

    private static PostCatalogRelation relation(Long id, Long postId, String catalogType, Long catalogId, String relationNote) {
        PostCatalogRelation relation = new PostCatalogRelation();
        relation.setId(id);
        relation.setPostId(postId);
        relation.setCatalogType(catalogType);
        relation.setCatalogId(catalogId);
        relation.setRelationNote(relationNote);
        return relation;
    }

    private static TarkovMap enabledMap() {
        TarkovMap map = new TarkovMap();
        map.setId(1L);
        map.setNameEn("Customs");
        map.setNameZh("Customs");
        map.setStatus("ENABLED");
        return map;
    }

    private static TarkovWeapon enabledWeapon() {
        TarkovWeapon weapon = new TarkovWeapon();
        weapon.setId(1L);
        weapon.setNameEn("AK-74N");
        weapon.setNameZh("AK-74N");
        weapon.setWeaponType("Assault rifle");
        weapon.setCaliber("5.45x39");
        weapon.setImageUrl("https://assets.example/ak-74n.png");
        weapon.setStatus("ENABLED");
        return weapon;
    }
}
