package com.wxm.wuyou.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// 标记该类为Spring组件，使其能够被Spring框架自动扫描和注入
@Component
// 标记该类为配置属性类，并指定属性前缀为"cookie"，用于从配置文件中读取以"cookie"开头的属性
@ConfigurationProperties(prefix = "cookie")
public class CookieConfigProperties {
    // 私有成员变量，用于存储cookie的密钥
    private String secret;

    // Getters and Setters
    // 公共方法，用于获取cookie的密钥
    public String getSecret() {
        return secret;
    }

    // 公共方法，用于设置cookie的密钥
    public void setSecret(String secret) {
        this.secret = secret;
    }
}
