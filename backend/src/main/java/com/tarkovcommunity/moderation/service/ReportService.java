package com.tarkovcommunity.moderation.service;

import com.tarkovcommunity.moderation.dto.ReportCreateRequest;
import com.tarkovcommunity.moderation.dto.ReportCreatedResponse;
import com.tarkovcommunity.user.entity.SysUser;

public interface ReportService {

    ReportCreatedResponse createReport(ReportCreateRequest request, SysUser reporter);
}
