package group.bison.xueyou.utils;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RedisConnectionTest {

    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> syncCommands;

    // 测试前的初始化
    @BeforeEach
    public void setUp() {
        // 读取 application.yml 配置
        String redisHost = "127.0.0.1";
        int redisPort = 6379;
        String redisPassword = ""; // 如果需要密码，请设置
        int redisDatabase = 0;

        try {
            // 创建 Redis 客户端
            redisClient = RedisClient.create("redis://" + redisHost + ":" + redisPort);
            connection = redisClient.connect();
            syncCommands = connection.sync();

            // 认证和选择数据库
            if (!redisPassword.isEmpty()) {
                syncCommands.auth(redisPassword);
            }
            syncCommands.select(redisDatabase);

            System.out.println("Redis 连接成功！");
        } catch (RedisCommandExecutionException e) {
            if (e.getMessage().contains("MISCONF")) {
                System.err.println("Redis 配置错误：持久化失败，请检查 Redis 日志和配置。");
            } else {
                System.err.println("Redis 命令执行失败：" + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("连接 Redis 失败：" + e.getMessage());
        }
    }


    // 测试方法
    @Test
    public void testRedisConnection() {
        // 测试连接
        syncCommands.set("testKey", "testValue");
        String value = syncCommands.get("testKey");

        // 验证结果
        assertEquals("testValue", value, "The value of 'testKey' should be 'testValue'");
    }

    // 测试后的清理工作
    @AfterEach
    public void tearDown() {
        // 删除测试键值对
        syncCommands.del("testKey");

        // 关闭连接
        connection.close();
        redisClient.shutdown();
    }
}
