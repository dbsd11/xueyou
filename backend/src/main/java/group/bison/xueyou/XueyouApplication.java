package group.bison.xueyou;

import group.bison.xueyou.config.AliyunSmsConfigProperties;
import group.bison.xueyou.config.CookieConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({CookieConfigProperties.class, AliyunSmsConfigProperties.class})
public class XueyouApplication {

    private static final Logger logger = LoggerFactory.getLogger(XueyouApplication.class);

    // 定义日志常量，避免硬编码字符串
    private static final String LOG_START_SUCCESS = "XueyouApplication started successfully.";
    private static final String LOG_START_FAILURE = "Failed to start XueyouApplication: {}";

    public static void main(String[] args) {
    // 使用try-catch块来捕获启动过程中的异常
        try {
        // 记录信息日志，表示应用程序开始启动
            logger.info("Starting XueyouApplication...");
        // 运行SpringApplication，启动Spring应用
            SpringApplication.run(XueyouApplication.class, args);
        // 记录信息日志，表示应用程序启动成功
            logger.info(LOG_START_SUCCESS);
        } catch (Exception e) {
            // 增强异常处理，保留原始异常信息
            logger.error(LOG_START_FAILURE, e.getMessage(), e);
            throw new RuntimeException("XueyouApplication failed to start", e);
        }
    }
}
