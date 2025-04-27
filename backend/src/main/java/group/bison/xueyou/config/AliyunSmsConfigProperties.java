package group.bison.xueyou.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// 标记该类为Spring组件，使其能够被Spring框架自动扫描和注入
@Component
// 标记该类为配置属性类，并指定属性前缀为"aliyun.sms"，用于从配置文件中注入属性值
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSmsConfigProperties {
    // 阿里云短信服务的访问密钥ID
    private String accessKeyId;
    // 阿里云短信服务的访问密钥
    private String accessKeySecret;
    // 短信签名名称
    private String signName;
    // 短信模板代码
    private String templateCode;

    // Getters and Setters
    public String getAccessKeyId() {
        return accessKeyId;
    }

// 定义一个公共的方法，用于设置访问密钥ID
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }
}
