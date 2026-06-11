package com.tarkovcommunity.forum.service;

import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.CommentCreateRequest;
import com.tarkovcommunity.forum.dto.CommentCreatedResponse;
import com.tarkovcommunity.forum.dto.CommentResponse;
import com.tarkovcommunity.forum.dto.CommentWithdrawResponse;
import com.tarkovcommunity.user.entity.SysUser;

public interface ForumCommentService {

    PageResponse<CommentResponse> listComments(Long postId, int page, int size, SysUser viewer);

    CommentCreatedResponse createComment(Long postId, CommentCreateRequest request, SysUser author);

    CommentWithdrawResponse withdrawComment(Long postId, Long commentId, SysUser author);
}
