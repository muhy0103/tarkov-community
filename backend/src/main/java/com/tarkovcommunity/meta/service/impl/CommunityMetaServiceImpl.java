package com.tarkovcommunity.meta.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tarkovcommunity.meta.dto.CategoryResponse;
import com.tarkovcommunity.meta.dto.TagResponse;
import com.tarkovcommunity.meta.entity.Category;
import com.tarkovcommunity.meta.entity.Tag;
import com.tarkovcommunity.meta.mapper.CategoryMapper;
import com.tarkovcommunity.meta.mapper.TagMapper;
import com.tarkovcommunity.meta.service.CommunityMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityMetaServiceImpl implements CommunityMetaService {

    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

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
}
