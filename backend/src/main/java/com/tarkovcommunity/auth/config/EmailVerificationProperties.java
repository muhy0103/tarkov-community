package com.tarkovcommunity.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.email-verification")
public class EmailVerificationProperties {
    private String frontendUrl = "http://127.0.0.1:5173";
    private String from = "nzdyhbzx@sina.com";
    private boolean enabled = true;
    private boolean devLinkEnabled = true;
    private int tokenMinutes = 30;
}
