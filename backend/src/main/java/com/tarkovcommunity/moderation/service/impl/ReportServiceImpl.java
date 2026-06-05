package com.tarkovcommunity.moderation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.entity.PostComment;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.moderation.dto.ReportCreateRequest;
import com.tarkovcommunity.moderation.dto.ReportCreatedResponse;
import com.tarkovcommunity.moderation.entity.Report;
import com.tarkovcommunity.moderation.mapper.ReportMapper;
import com.tarkovcommunity.moderation.service.ReportService;
import com.tarkovcommunity.user.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final Set<String> TARGET_TYPES = Set.of("POST", "COMMENT");

    private final ReportMapper reportMapper;
    private final PostMapper postMapper;
    private final PostCommentMapper commentMapper;

    @Override
    public ReportCreatedResponse createReport(ReportCreateRequest request, SysUser reporter) {
        String targetType = normalizeTargetType(request.targetType());
        validateTargetExists(targetType, request.targetId());

        Report existingReport = reportMapper.selectOne(new LambdaQueryWrapper<Report>()
                .eq(Report::getReporterId, reporter.getId())
                .eq(Report::getTargetType, targetType)
                .eq(Report::getTargetId, request.targetId()));
        if (existingReport != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "已经举报过该内容");
        }

        Report report = new Report();
        report.setReporterId(reporter.getId());
        report.setTargetType(targetType);
        report.setTargetId(request.targetId());
        report.setReason(request.reason().trim());
        report.setDescription(trimToEmpty(request.description()));
        report.setStatus("PENDING");
        reportMapper.insert(report);

        return new ReportCreatedResponse(
                report.getId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }

    private void validateTargetExists(String targetType, Long targetId) {
        if ("POST".equals(targetType)) {
            Post post = postMapper.selectById(targetId);
            if (post == null || isDeleted(post.getDeleted())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "举报的帖子不存在");
            }
            return;
        }

        PostComment comment = commentMapper.selectById(targetId);
        if (comment == null || isDeleted(comment.getDeleted())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "举报的评论不存在");
        }
    }

    private static String normalizeTargetType(String targetType) {
        String normalized = targetType == null ? "" : targetType.trim().toUpperCase();
        if (!TARGET_TYPES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "举报目标类型不正确");
        }
        return normalized;
    }

    private static boolean isDeleted(Integer deleted) {
        return deleted != null && deleted == 1;
    }

    private static String trimToEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}
