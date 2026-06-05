package com.tarkovcommunity.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminReportResponse;
import com.tarkovcommunity.admin.dto.AdminReportReviewRequest;
import com.tarkovcommunity.admin.service.impl.AdminReportServiceImpl;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.entity.PostComment;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.moderation.entity.Report;
import com.tarkovcommunity.moderation.mapper.ReportMapper;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceImplTests {

    @Mock
    private ReportMapper reportMapper;

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostCommentMapper commentMapper;

    @Test
    void listsReportsWithReporterAndTargetSummary() {
        AdminReportServiceImpl service = new AdminReportServiceImpl(reportMapper, userMapper, postMapper, commentMapper);
        Page<Report> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(pendingPostReport()));
        given(reportMapper.selectPage(any(), any())).willReturn(page);
        given(userMapper.selectBatchIds(any())).willReturn(List.of(reporter()));
        given(postMapper.selectBatchIds(List.of(2L))).willReturn(List.of(post()));

        PageResponse<AdminReportResponse> response = service.listReports("PENDING", "POST", "广告", 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).singleElement()
                .satisfies(report -> {
                    assertThat(report.reporterNickname()).isEqualTo("新手PMC");
                    assertThat(report.targetTitle()).isEqualTo("海关任务路线");
                    assertThat(report.targetSummary()).isEqualTo("这是一篇需要处理的帖子。");
                    assertThat(report.status()).isEqualTo("PENDING");
                });
    }

    @Test
    void reviewsReportAndRecordsHandler() {
        AdminReportServiceImpl service = new AdminReportServiceImpl(reportMapper, userMapper, postMapper, commentMapper);
        given(reportMapper.selectById(1L)).willReturn(pendingPostReport(), resolvedPostReport());
        given(userMapper.selectBatchIds(any())).willReturn(List.of(reporter(), admin()));
        given(postMapper.selectBatchIds(List.of(2L))).willReturn(List.of(post()));

        AdminReportResponse response = service.reviewReport(1L, new AdminReportReviewRequest(
                "RESOLVED",
                " 已隐藏违规内容。 ",
                false
        ), admin());

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportMapper).updateById(reportCaptor.capture());
        verify(postMapper, never()).updateById(any(Post.class));
        verify(commentMapper, never()).updateById(any(PostComment.class));
        Report savedReport = reportCaptor.getValue();
        assertThat(savedReport.getStatus()).isEqualTo("RESOLVED");
        assertThat(savedReport.getHandlerId()).isEqualTo(9L);
        assertThat(savedReport.getHandleResult()).isEqualTo("已隐藏违规内容。");
        assertThat(savedReport.getHandledAt()).isNotNull();
        assertThat(response.status()).isEqualTo("RESOLVED");
        assertThat(response.handlerNickname()).isEqualTo("管理员");
    }

    @Test
    void resolvesReportAndHidesPostTargetWhenRequested() {
        AdminReportServiceImpl service = new AdminReportServiceImpl(reportMapper, userMapper, postMapper, commentMapper);
        given(reportMapper.selectById(1L)).willReturn(pendingPostReport(), resolvedPostReport());
        given(postMapper.updateById(any(Post.class))).willReturn(1);
        given(userMapper.selectBatchIds(any())).willReturn(List.of(reporter(), admin()));
        given(postMapper.selectBatchIds(List.of(2L))).willReturn(List.of(hiddenPost()));

        service.reviewReport(1L, new AdminReportReviewRequest(
                "RESOLVED",
                "违规广告已隐藏。",
                true
        ), admin());

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).updateById(postCaptor.capture());
        assertThat(postCaptor.getValue().getId()).isEqualTo(2L);
        assertThat(postCaptor.getValue().getStatus()).isEqualTo("HIDDEN");
    }

    @Test
    void resolvesReportAndHidesCommentTargetWhenRequested() {
        AdminReportServiceImpl service = new AdminReportServiceImpl(reportMapper, userMapper, postMapper, commentMapper);
        given(reportMapper.selectById(1L)).willReturn(pendingCommentReport(), resolvedCommentReport());
        given(commentMapper.updateById(any(PostComment.class))).willReturn(1);
        given(userMapper.selectBatchIds(any())).willReturn(List.of(reporter(), admin()));
        given(commentMapper.selectBatchIds(List.of(3L))).willReturn(List.of(hiddenComment()));
        given(postMapper.selectBatchIds(List.of(2L))).willReturn(List.of(post()));

        service.reviewReport(1L, new AdminReportReviewRequest(
                "RESOLVED",
                "违规评论已隐藏。",
                true
        ), admin());

        ArgumentCaptor<PostComment> commentCaptor = ArgumentCaptor.forClass(PostComment.class);
        verify(commentMapper).updateById(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getId()).isEqualTo(3L);
        assertThat(commentCaptor.getValue().getStatus()).isEqualTo("HIDDEN");
    }

    private static Report pendingPostReport() {
        Report report = new Report();
        report.setId(1L);
        report.setReporterId(7L);
        report.setTargetType("POST");
        report.setTargetId(2L);
        report.setReason("广告刷屏");
        report.setDescription("帖子中存在交易广告。");
        report.setStatus("PENDING");
        report.setCreatedAt(LocalDateTime.of(2026, 6, 5, 16, 0));
        return report;
    }

    private static Report pendingCommentReport() {
        Report report = pendingPostReport();
        report.setTargetType("COMMENT");
        report.setTargetId(3L);
        return report;
    }

    private static Report resolvedPostReport() {
        Report report = pendingPostReport();
        report.setStatus("RESOLVED");
        report.setHandlerId(9L);
        report.setHandleResult("已隐藏违规内容。");
        report.setHandledAt(LocalDateTime.of(2026, 6, 5, 16, 30));
        return report;
    }

    private static Report resolvedCommentReport() {
        Report report = pendingCommentReport();
        report.setStatus("RESOLVED");
        report.setHandlerId(9L);
        report.setHandleResult("违规评论已隐藏。");
        report.setHandledAt(LocalDateTime.of(2026, 6, 5, 16, 30));
        return report;
    }

    private static Post post() {
        Post post = new Post();
        post.setId(2L);
        post.setTitle("海关任务路线");
        post.setContent("这是一篇需要处理的帖子。");
        return post;
    }

    private static Post hiddenPost() {
        Post post = post();
        post.setStatus("HIDDEN");
        return post;
    }

    private static PostComment comment() {
        PostComment comment = new PostComment();
        comment.setId(3L);
        comment.setPostId(2L);
        comment.setContent("这是一条需要处理的评论。");
        return comment;
    }

    private static PostComment hiddenComment() {
        PostComment comment = comment();
        comment.setStatus("HIDDEN");
        return comment;
    }

    private static SysUser reporter() {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setNickname("新手PMC");
        return user;
    }

    private static SysUser admin() {
        SysUser user = new SysUser();
        user.setId(9L);
        user.setNickname("管理员");
        user.setRole("ADMIN");
        return user;
    }
}
