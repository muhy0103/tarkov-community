package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminReportResponse;
import com.tarkovcommunity.admin.dto.AdminReportReviewRequest;
import com.tarkovcommunity.admin.service.AdminReportService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.entity.PostComment;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.moderation.entity.Report;
import com.tarkovcommunity.moderation.mapper.ReportMapper;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final Set<String> ALLOWED_STATUSES = Set.of("PENDING", "RESOLVED", "REJECTED");

    private final ReportMapper reportMapper;
    private final SysUserMapper userMapper;
    private final PostMapper postMapper;
    private final PostCommentMapper commentMapper;

    @Override
    public PageResponse<AdminReportResponse> listReports(
            String status,
            String targetType,
            String keyword,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<Report> query = new LambdaQueryWrapper<Report>()
                .orderByDesc(Report::getCreatedAt)
                .orderByDesc(Report::getId);

        if (StringUtils.hasText(status)) {
            query.eq(Report::getStatus, status);
        }

        if (StringUtils.hasText(targetType)) {
            query.eq(Report::getTargetType, targetType.trim().toUpperCase());
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(Report::getReason, keyword)
                    .or()
                    .like(Report::getDescription, keyword));
        }

        Page<Report> reportPage = reportMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, reportPage.getTotal(), toResponses(reportPage.getRecords()));
    }

    @Override
    @Transactional
    public AdminReportResponse reviewReport(Long id, AdminReportReviewRequest request, SysUser admin) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "举报记录不存在");
        }

        if (!ALLOWED_STATUSES.contains(request.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "举报处理状态不正确");
        }

        hideTargetIfRequested(report, request);

        report.setStatus(request.status());
        report.setHandleResult(trimToEmpty(request.handleResult()));
        if ("PENDING".equals(request.status())) {
            report.setHandlerId(null);
            report.setHandledAt(null);
        } else {
            report.setHandlerId(admin.getId());
            report.setHandledAt(LocalDateTime.now());
        }
        reportMapper.updateById(report);

        Report updatedReport = reportMapper.selectById(id);
        return toResponses(List.of(updatedReport == null ? report : updatedReport)).get(0);
    }

    private void hideTargetIfRequested(Report report, AdminReportReviewRequest request) {
        if (!"RESOLVED".equals(request.status()) || !Boolean.TRUE.equals(request.hideTarget())) {
            return;
        }

        if ("POST".equals(report.getTargetType())) {
            Post post = new Post();
            post.setId(report.getTargetId());
            post.setStatus("HIDDEN");
            ensureUpdated(postMapper.updateById(post), "被举报帖子不存在，无法隐藏");
            return;
        }

        if ("COMMENT".equals(report.getTargetType())) {
            PostComment comment = commentMapper.selectById(report.getTargetId());
            if (comment == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "被举报评论不存在，无法隐藏");
            }

            String oldStatus = comment.getStatus();
            comment.setStatus("HIDDEN");
            ensureUpdated(commentMapper.updateById(comment), "被举报评论不存在，无法隐藏");
            adjustPostCommentCount(comment.getPostId(), oldStatus, "HIDDEN");
            return;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "举报目标类型不支持联动隐藏");
    }

    private void adjustPostCommentCount(Long postId, String oldStatus, String newStatus) {
        if (Objects.equals(oldStatus, newStatus)) {
            return;
        }

        boolean wasVisible = "NORMAL".equals(oldStatus);
        boolean isVisible = "NORMAL".equals(newStatus);
        if (wasVisible == isVisible) {
            return;
        }

        Post post = postMapper.selectById(postId);
        if (post == null) {
            return;
        }

        int currentCount = valueOrZero(post.getCommentCount());
        post.setCommentCount(isVisible ? currentCount + 1 : Math.max(0, currentCount - 1));
        postMapper.updateById(post);
    }

    private static void ensureUpdated(int affectedRows, String message) {
        if (affectedRows <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
    }

    private List<AdminReportResponse> toResponses(List<Report> reports) {
        if (reports.isEmpty()) {
            return List.of();
        }

        Map<Long, SysUser> users = selectByIds(
                Stream.concat(
                                reports.stream().map(Report::getReporterId),
                                reports.stream().map(Report::getHandlerId)
                        )
                        .filter(Objects::nonNull)
                        .toList(),
                userMapper::selectBatchIds,
                SysUser::getId
        );

        Map<Long, Post> postTargets = selectByIds(
                reports.stream()
                        .filter(report -> "POST".equals(report.getTargetType()))
                        .map(Report::getTargetId)
                        .filter(Objects::nonNull)
                        .toList(),
                postMapper::selectBatchIds,
                Post::getId
        );

        Map<Long, PostComment> commentTargets = selectByIds(
                reports.stream()
                        .filter(report -> "COMMENT".equals(report.getTargetType()))
                        .map(Report::getTargetId)
                        .filter(Objects::nonNull)
                        .toList(),
                commentMapper::selectBatchIds,
                PostComment::getId
        );

        Map<Long, Post> commentPosts = selectByIds(
                commentTargets.values().stream()
                        .map(PostComment::getPostId)
                        .filter(Objects::nonNull)
                        .toList(),
                postMapper::selectBatchIds,
                Post::getId
        );

        return reports.stream()
                .map(report -> toResponse(report, users, postTargets, commentTargets, commentPosts))
                .toList();
    }

    private static AdminReportResponse toResponse(
            Report report,
            Map<Long, SysUser> users,
            Map<Long, Post> postTargets,
            Map<Long, PostComment> commentTargets,
            Map<Long, Post> commentPosts
    ) {
        SysUser reporter = users.get(report.getReporterId());
        SysUser handler = users.get(report.getHandlerId());
        TargetView target = resolveTarget(report, postTargets, commentTargets, commentPosts);

        return new AdminReportResponse(
                report.getId(),
                report.getReporterId(),
                reporter == null ? "未知玩家" : reporter.getNickname(),
                report.getTargetType(),
                report.getTargetId(),
                target.title(),
                target.summary(),
                report.getReason(),
                report.getDescription(),
                report.getStatus(),
                report.getHandlerId(),
                handler == null ? "未处理" : handler.getNickname(),
                report.getHandleResult(),
                report.getHandledAt(),
                report.getCreatedAt()
        );
    }

    private static TargetView resolveTarget(
            Report report,
            Map<Long, Post> postTargets,
            Map<Long, PostComment> commentTargets,
            Map<Long, Post> commentPosts
    ) {
        if ("POST".equals(report.getTargetType())) {
            Post post = postTargets.get(report.getTargetId());
            return new TargetView(post == null ? "未知帖子" : post.getTitle(), post == null ? "" : post.getContent());
        }

        PostComment comment = commentTargets.get(report.getTargetId());
        if (comment == null) {
            return new TargetView("未知评论", "");
        }

        Post post = commentPosts.get(comment.getPostId());
        String title = post == null ? "评论所属帖子未知" : "评论所属帖子：" + post.getTitle();
        return new TargetView(title, comment.getContent());
    }

    private static <T> Map<Long, T> selectByIds(
            List<Long> ids,
            Function<List<Long>, List<T>> selector,
            Function<T, Long> idGetter
    ) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return selector.apply(ids.stream().distinct().toList())
                .stream()
                .collect(Collectors.toMap(idGetter, Function.identity()));
    }

    private static String trimToEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private record TargetView(String title, String summary) {
    }
}
