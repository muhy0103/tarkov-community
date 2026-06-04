package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminCategoryResponse;
import com.tarkovcommunity.admin.dto.AdminCategoryUpdateRequest;
import com.tarkovcommunity.admin.service.AdminCategoryService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.meta.entity.Category;
import com.tarkovcommunity.meta.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private static final int MAX_PAGE_SIZE = 50;

    private final CategoryMapper categoryMapper;

    @Override
    public PageResponse<AdminCategoryResponse> listCategories(String status, String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<Category> query = new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId);

        if (StringUtils.hasText(status)) {
            query.eq(Category::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(Category::getName, keyword)
                    .or()
                    .like(Category::getCode, keyword)
                    .or()
                    .like(Category::getDescription, keyword));
        }

        Page<Category> categoryPage = categoryMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, categoryPage.getTotal(), toResponses(categoryPage.getRecords()));
    }

    @Override
    public AdminCategoryResponse updateCategory(Long id, AdminCategoryUpdateRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "分区不存在");
        }

        category.setName(request.name().trim());
        category.setDescription(normalizeNullable(request.description()));
        category.setIcon(normalizeNullable(request.icon()));
        category.setSortOrder(request.sortOrder());
        category.setStatus(request.status());
        categoryMapper.updateById(category);

        return toResponse(categoryMapper.selectById(id));
    }

    private static List<AdminCategoryResponse> toResponses(List<Category> categories) {
        return categories.stream()
                .map(AdminCategoryServiceImpl::toResponse)
                .toList();
    }

    private static AdminCategoryResponse toResponse(Category category) {
        return new AdminCategoryResponse(
                category.getId(),
                category.getName(),
                category.getCode(),
                category.getDescription(),
                category.getIcon(),
                category.getSortOrder(),
                category.getStatus()
        );
    }

    private static String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
