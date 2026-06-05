package com.tarkovcommunity.moderation;

import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.entity.PostComment;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.moderation.dto.ReportCreateRequest;
import com.tarkovcommunity.moderation.dto.ReportCreatedResponse;
import com.tarkovcommunity.moderation.entity.Report;
import com.tarkovcommunity.moderation.mapper.ReportMapper;
import com.tarkovcommunity.moderation.service.impl.ReportServiceImpl;
import com.tarkovcommunity.user.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTests {

    @Mock
    private ReportMapper reportMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostCommentMapper commentMapper;

    @Test
    void createsPostReportWhenTargetExists() {
        ReportServiceImpl service = new ReportServiceImpl(reportMapper, postMapper, commentMapper);
        given(postMapper.selectById(2L)).willReturn(post());

        ReportCreatedResponse response = service.createReport(new ReportCreateRequest(
                "POST",
                2L,
                " 广告刷屏 ",
                " 帖子中存在交易广告。 "
        ), normalUser());

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportMapper).insert(reportCaptor.capture());
        Report savedReport = reportCaptor.getValue();
        assertThat(savedReport.getReporterId()).isEqualTo(7L);
        assertThat(savedReport.getTargetType()).isEqualTo("POST");
        assertThat(savedReport.getTargetId()).isEqualTo(2L);
        assertThat(savedReport.getReason()).isEqualTo("广告刷屏");
        assertThat(savedReport.getDescription()).isEqualTo("帖子中存在交易广告。");
        assertThat(savedReport.getStatus()).isEqualTo("PENDING");
        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void createsCommentReportWhenTargetExists() {
        ReportServiceImpl service = new ReportServiceImpl(reportMapper, postMapper, commentMapper);
        given(commentMapper.selectById(3L)).willReturn(comment());

        service.createReport(new ReportCreateRequest(
                "COMMENT",
                3L,
                "人身攻击",
                ""
        ), normalUser());

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportMapper).insert(reportCaptor.capture());
        assertThat(reportCaptor.getValue().getTargetType()).isEqualTo("COMMENT");
        assertThat(reportCaptor.getValue().getTargetId()).isEqualTo(3L);
    }

    @Test
    void rejectsDuplicateReport() {
        ReportServiceImpl service = new ReportServiceImpl(reportMapper, postMapper, commentMapper);
        given(postMapper.selectById(2L)).willReturn(post());
        given(reportMapper.selectOne(any())).willReturn(existingReport());

        assertThatThrownBy(() -> service.createReport(new ReportCreateRequest(
                "POST",
                2L,
                "广告刷屏",
                ""
        ), normalUser()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void rejectsMissingReportTarget() {
        ReportServiceImpl service = new ReportServiceImpl(reportMapper, postMapper, commentMapper);

        assertThatThrownBy(() -> service.createReport(new ReportCreateRequest(
                "POST",
                99L,
                "广告刷屏",
                ""
        ), normalUser()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void rejectsHiddenPostReportTarget() {
        ReportServiceImpl service = new ReportServiceImpl(reportMapper, postMapper, commentMapper);
        given(postMapper.selectById(2L)).willReturn(post("HIDDEN"));

        assertThatThrownBy(() -> service.createReport(new ReportCreateRequest(
                "POST",
                2L,
                "广告刷屏",
                ""
        ), normalUser()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
        verify(reportMapper, never()).insert(any(Report.class));
    }

    @Test
    void rejectsHiddenCommentReportTarget() {
        ReportServiceImpl service = new ReportServiceImpl(reportMapper, postMapper, commentMapper);
        given(commentMapper.selectById(3L)).willReturn(comment("HIDDEN"));

        assertThatThrownBy(() -> service.createReport(new ReportCreateRequest(
                "COMMENT",
                3L,
                "人身攻击",
                ""
        ), normalUser()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
        verify(reportMapper, never()).insert(any(Report.class));
    }

    private static Report existingReport() {
        Report report = new Report();
        report.setId(5L);
        report.setReporterId(7L);
        report.setTargetType("POST");
        report.setTargetId(2L);
        report.setReason("广告刷屏");
        report.setStatus("PENDING");
        return report;
    }

    private static Post post() {
        return post("NORMAL");
    }

    private static Post post(String status) {
        Post post = new Post();
        post.setId(2L);
        post.setTitle("海关任务路线");
        post.setStatus(status);
        post.setDeleted(0);
        return post;
    }

    private static PostComment comment() {
        return comment("NORMAL");
    }

    private static PostComment comment(String status) {
        PostComment comment = new PostComment();
        comment.setId(3L);
        comment.setPostId(2L);
        comment.setContent("这个评论需要处理。");
        comment.setStatus(status);
        comment.setDeleted(0);
        return comment;
    }

    private static SysUser normalUser() {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setNickname("新手PMC");
        return user;
    }
}
