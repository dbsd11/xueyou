package group.bison.xueyou.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.embedded.RedisServer;
import redis.embedded.RedisServerBuilder;

import java.io.IOException;

@Configuration
@DependsOn("redisServer")
public class RedisConfig {
    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Bean("redisServer")
    public static RedisServer redisServer() throws IOException {
        RedisServer redisServer = new RedisServerBuilder()
                .setting("maxheap 256m")
                .port(6379).build();
        try {
            redisServer.start();
        } catch (Exception e) {
            logger.warn("failed start redis");
        }
        return redisServer;
    }

    /**
     * 创建一个 RedisTemplate Bean，用于操作 Redis 数据库
     *
     * @return 配置好的 RedisTemplate 实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 创建一个 RedisTemplate 实例，泛型参数为键和值的类型
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置 Redis 连接工厂，用于创建与 Redis 服务器的连接
        template.setConnectionFactory(redisConnectionFactory);
        // 设置键的序列化器，使用 StringRedisSerializer 将键序列化为字符串
        template.setKeySerializer(new StringRedisSerializer());
        // 设置值的序列化器，使用 StringRedisSerializer 将值序列化为字符串
        template.setValueSerializer(new StringRedisSerializer());
        // 设置哈希键的序列化器，使用 StringRedisSerializer 将哈希键序列化为字符串
        template.setHashKeySerializer(new StringRedisSerializer());
        // 设置哈希值的序列化器，使用 StringRedisSerializer 将哈希值序列化为字符串
        template.setHashValueSerializer(new StringRedisSerializer());
        // 初始化 RedisTemplate，确保所有属性都已正确设置
        template.afterPropertiesSet();
        // 返回配置好的 RedisTemplate 实例
        return template;
    }
}
