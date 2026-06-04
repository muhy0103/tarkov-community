package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminItemResponse;
import com.tarkovcommunity.admin.dto.AdminItemUpdateRequest;
import com.tarkovcommunity.admin.service.AdminItemService;
import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/items")
public class AdminItemController {

    private final AdminItemService adminItemService;

    @GetMapping
    public ApiResponse<PageResponse<AdminItemResponse>> listItems(
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean questNeeded,
            @RequestParam(required = false) Boolean hideoutNeeded,
            @RequestParam(required = false) Boolean keepSuggestion,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminItemService.listItems(
                itemType,
                status,
                questNeeded,
                hideoutNeeded,
                keepSuggestion,
                keyword,
                page,
                size
        ));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminItemResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody AdminItemUpdateRequest request
    ) {
        return ApiResponse.success(adminItemService.updateItem(id, request));
    }
}
