package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminTagResponse;
import com.tarkovcommunity.admin.dto.AdminTagUpdateRequest;
import com.tarkovcommunity.admin.service.AdminTagService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.meta.entity.Tag;
import com.tarkovcommunity.meta.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTagServiceImpl implements AdminTagService {

    private static final int MAX_PAGE_SIZE = 50;

    private final TagMapper tagMapper;

    @Override
    public PageResponse<AdminTagResponse> listTags(String type, String status, String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<Tag> query = new LambdaQueryWrapper<Tag>()
                .orderByAsc(Tag::getType)
                .orderByAsc(Tag::getName)
                .orderByAsc(Tag::getId);

        if (StringUtils.hasText(type)) {
            query.eq(Tag::getType, type);
        }

        if (StringUtils.hasText(status)) {
            query.eq(Tag::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(Tag::getName, keyword)
                    .or()
                    .like(Tag::getType, keyword)
                    .or()
                    .like(Tag::getColor, keyword));
        }

        Page<Tag> tagPage = tagMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, tagPage.getTotal(), toResponses(tagPage.getRecords()));
    }

    @Override
    public AdminTagResponse updateTag(Long id, AdminTagUpdateRequest request) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "标签不存在");
        }

        tag.setName(request.name().trim());
        tag.setType(request.type().trim());
        tag.setColor(normalizeNullable(request.color()));
        tag.setStatus(request.status());
        tagMapper.updateById(tag);

        return toResponse(tagMapper.selectById(id));
    }

    private static List<AdminTagResponse> toResponses(List<Tag> tags) {
        return tags.stream()
                .map(AdminTagServiceImpl::toResponse)
                .toList();
    }

    private static AdminTagResponse toResponse(Tag tag) {
        return new AdminTagResponse(
                tag.getId(),
                tag.getName(),
                tag.getType(),
                tag.getColor(),
                tag.getStatus()
        );
    }

    private static String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
