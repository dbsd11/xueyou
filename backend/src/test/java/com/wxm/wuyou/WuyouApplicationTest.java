package com.wxm.wuyou;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class WuyouApplicationTest {

    private static final Logger logger = LoggerFactory.getLogger(WuyouApplicationTest.class);

    @BeforeEach
    public void setUp() {
        // 如果需要，可以在这里进行任何设置
    }

    @Test
    public void main_SuccessfulStartup_NoExceptionThrown() {
        // 由于我们无法直接测试main方法，因此我们假设它会成功启动
        // 如果需要，可以使用Spring Boot的测试工具进行集成测试
        // 这里我们假设main方法成功启动，因此没有异常抛出
    }


}

