package com.tarkovcommunity.meta.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.meta.dto.AnnouncementResponse;
import com.tarkovcommunity.meta.dto.CategoryResponse;
import com.tarkovcommunity.meta.dto.TagResponse;
import com.tarkovcommunity.meta.entity.Announcement;
import com.tarkovcommunity.meta.entity.Category;
import com.tarkovcommunity.meta.entity.Tag;
import com.tarkovcommunity.meta.mapper.AnnouncementMapper;
import com.tarkovcommunity.meta.mapper.CategoryMapper;
import com.tarkovcommunity.meta.mapper.TagMapper;
import com.tarkovcommunity.meta.service.CommunityMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityMetaServiceImpl implements CommunityMetaService {

    private static final int MAX_PAGE_SIZE = 20;

    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final AnnouncementMapper announcementMapper;

    @Override
    public List<CategoryResponse> listCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, "ENABLED")
                        .orderByAsc(Category::getSortOrder)
                        .orderByAsc(Category::getId))
                .stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getCode(),
                        category.getDescription(),
                        category.getIcon()
                ))
                .toList();
    }

    @Override
    public List<TagResponse> listTags() {
        return tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getStatus, "ENABLED")
                        .orderByAsc(Tag::getType)
                        .orderByAsc(Tag::getId))
                .stream()
                .map(tag -> new TagResponse(
                        tag.getId(),
                        tag.getName(),
                        tag.getType(),
                        tag.getColor()
                ))
                .toList();
    }

    @Override
    public PageResponse<AnnouncementResponse> listPublishedAnnouncements(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        Page<Announcement> announcementPage = announcementMapper.selectPage(
                new Page<>(safePage, safeSize),
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getStatus, "PUBLISHED")
                        .orderByDesc(Announcement::getCreatedAt)
                        .orderByDesc(Announcement::getId)
        );

        return PageResponse.of(
                safePage,
                safeSize,
                announcementPage.getTotal(),
                announcementPage.getRecords().stream()
                        .map(announcement -> new AnnouncementResponse(
                                announcement.getId(),
                                announcement.getTitle(),
                                announcement.getContent(),
                                announcement.getCreatedAt(),
                                announcement.getUpdatedAt()
                        ))
                        .toList()
        );
    }
}
