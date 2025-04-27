package com.wxm.wuyou.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.wxm.wuyou.interceptor.AuthInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    // 自动注入AuthInterceptor拦截器
    @Autowired
    private AuthInterceptor authInterceptor;

    // 注入环境变量，用于读取配置文件中的路径
    @Autowired
    private Environment environment;

    @Value("${cors.allowedOrigins:http://localhost:8080,http://localhost:18888}")
    private String[] corsAllowedOrigins;

    private static final String DEFAULT_TIME_PATTERN = "HH:mm";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 对所有路径应用跨域配置
                .allowedOrigins(corsAllowedOrigins)  // 允许的源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")  // 允许的方法
                .allowedHeaders("*")  // 允许的头信息
                .allowCredentials(true)  // 是否发送Cookie等凭证信息
                .maxAge(3600);  // 预检请求的缓存时间（秒）
    }

    // 重写addInterceptors方法，用于注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 健壮性检查：确保authInterceptor不为null
        if (authInterceptor == null) {
            throw new IllegalStateException("AuthInterceptor bean is not properly configured.");
        }

        // 从配置文件中读取拦截路径和排除路径
        String includePatterns = environment.getProperty("web.interceptor.includePatterns", "/api/**");
        String excludePatterns = environment.getProperty("web.interceptor.excludePatterns", "");

        // 将排除路径拆分为数组
        String[] excludePathArray = excludePatterns.split(",");

        // 添加authInterceptor拦截器
        registry.addInterceptor(authInterceptor)
                // 设置拦截路径
                .addPathPatterns(includePatterns)
                // 设置排除路径
                .excludePathPatterns(excludePathArray);

        // 记录日志，方便调试
        logger.info("Registered AuthInterceptor with include patterns: " + includePatterns);
        logger.info("Excluded paths: " + Arrays.toString(excludePathArray));
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = converter.getObjectMapper();
        // 生成JSON时,将所有Date转换成Long
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE));

        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ISO_LOCAL_TIME));

        // 把“忽略重复的模块注册”禁用，否则下面的注册不生效
        objectMapper.disable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);
        objectMapper.registerModule(javaTimeModule);
        // 然后再设置为生效，避免被其他地方覆盖
        objectMapper.enable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);

        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 设置格式化内容
        converter.setObjectMapper(objectMapper);
        converters.add(0, converter);
    }

    @Bean
    public Converter<String, LocalDateTime> localDateTimeConverter() {
        return new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                return StringUtils.isEmpty(source) ? null :LocalDateTime.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        };
    }
}
