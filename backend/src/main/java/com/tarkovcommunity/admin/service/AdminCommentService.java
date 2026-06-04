package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminCommentResponse;
import com.tarkovcommunity.admin.dto.AdminCommentReviewRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminCommentService {

    PageResponse<AdminCommentResponse> listComments(
            String status,
            String keyword,
            int page,
            int size
    );

    AdminCommentResponse reviewComment(Long id, AdminCommentReviewRequest request);
}
