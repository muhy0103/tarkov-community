package com.tarkovcommunity.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.tarkovcommunity.forum.service.ForumCommentService;
import com.tarkovcommunity.forum.service.impl.ForumCommentServiceImpl;
import com.tarkovcommunity.forum.service.ForumPostService;
import com.tarkovcommunity.forum.service.ForumReactionService;
import com.tarkovcommunity.forum.service.impl.ForumPostServiceImpl;
import com.tarkovcommunity.forum.service.impl.ForumReactionServiceImpl;
import com.tarkovcommunity.meta.service.CommunityMetaService;
import com.tarkovcommunity.meta.service.impl.CommunityMetaServiceImpl;
import com.tarkovcommunity.tarkov.service.TarkovCatalogService;
import com.tarkovcommunity.tarkov.service.impl.TarkovCatalogServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MybatisPlusConfigTests {

    @Autowired(required = false)
    private MybatisPlusInterceptor mybatisPlusInterceptor;

    @Test
    void registersMybatisPlusInterceptor() {
        assertThat(mybatisPlusInterceptor).isNotNull();
    }

    @Autowired
    private CommunityMetaService communityMetaService;

    @Autowired
    private TarkovCatalogService tarkovCatalogService;

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private ForumCommentService forumCommentService;

    @Autowired
    private ForumReactionService forumReactionService;

    @Test
    void mapperScanDoesNotRegisterServiceInterfacesAsMappers() {
        assertThat(communityMetaService).isInstanceOf(CommunityMetaServiceImpl.class);
        assertThat(tarkovCatalogService).isInstanceOf(TarkovCatalogServiceImpl.class);
        assertThat(forumPostService).isInstanceOf(ForumPostServiceImpl.class);
        assertThat(forumCommentService).isInstanceOf(ForumCommentServiceImpl.class);
        assertThat(forumReactionService).isInstanceOf(ForumReactionServiceImpl.class);
    }
}
