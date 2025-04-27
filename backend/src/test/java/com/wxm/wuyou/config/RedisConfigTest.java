package com.wxm.wuyou.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisConfigTest {

    @InjectMocks
    private RedisConfig redisConfig;

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @Test
    void redisTemplate_WhenCalled_ShouldReturnConfiguredRedisTemplate() {
        // 调用被测试的方法
        RedisTemplate<String, Object> result = redisConfig.redisTemplate(redisConnectionFactory);

        // 修复 assertNotNull 的调用
        assertNotNull(result, "RedisTemplate should not be null");

        // 使用 spy 来监控实际的 RedisTemplate 实例
        RedisTemplate<String, Object> spiedResult = spy(result);

        // 验证序列化器类型
        assertSerializerType(spiedResult.getKeySerializer(), StringRedisSerializer.class, "Key serializer should be StringRedisSerializer");
        assertSerializerType(spiedResult.getValueSerializer(), StringRedisSerializer.class, "Value serializer should be StringRedisSerializer");
        assertSerializerType(spiedResult.getHashKeySerializer(), StringRedisSerializer.class, "Hash key serializer should be StringRedisSerializer");
        assertSerializerType(spiedResult.getHashValueSerializer(), StringRedisSerializer.class, "Hash value serializer should be StringRedisSerializer");

        // 验证 afterPropertiesSet() 是否被调用
        verify(spiedResult, times(1)).afterPropertiesSet();
    }

    @Test
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    // 封装断言逻辑，增强复用性和可读性
    private void assertSerializerType(Object serializer, Class<?> expectedType, String message) {
        // 检查是否为 null，避免 NullPointerException
        assertNotNull(serializer, "Serializer cannot be null");
        // 断言类型是否正确
        assertInstanceOf(expectedType, serializer, message);
    }
}
