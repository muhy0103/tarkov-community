package com.tarkovcommunity.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MybatisPlusConfigTests {

    @Autowired(required = false)
    private MybatisPlusInterceptor mybatisPlusInterceptor;

    @Test
    void registersMybatisPlusInterceptor() {
        assertThat(mybatisPlusInterceptor).isNotNull();
    }
}
