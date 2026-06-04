package com.tarkovcommunity.health;

import com.tarkovcommunity.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, Object>> check() {
        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", "tarkov-community-backend",
                "time", OffsetDateTime.now().toString()
        ));
    }
}
