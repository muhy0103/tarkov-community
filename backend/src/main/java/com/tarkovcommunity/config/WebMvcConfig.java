package com.tarkovcommunity.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnBean(AdminAccessInterceptor.class)
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminAccessInterceptor adminAccessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAccessInterceptor)
                .addPathPatterns("/api/admin/**");
    }
}
