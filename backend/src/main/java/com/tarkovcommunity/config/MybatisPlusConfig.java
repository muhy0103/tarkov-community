package com.tarkovcommunity.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({
        "com.tarkovcommunity.auth.mapper",
        "com.tarkovcommunity.meta.mapper",
        "com.tarkovcommunity.tarkov.mapper",
        "com.tarkovcommunity.user.mapper",
        "com.tarkovcommunity.forum.mapper",
        "com.tarkovcommunity.moderation.mapper",
        "com.tarkovcommunity.notification.mapper"
})
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
