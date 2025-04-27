package group.bison.xueyou.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfig {

    @Bean
    public OSS ossMinioClient(@Value("${oss.endpoint:}") String endpoint, @Value("${oss.accessKey:}") String accessKey, @Value("${oss.secretKey:}") String secretKey) {
        String region = endpoint.substring(endpoint.indexOf("-") + 1, endpoint.indexOf("."));

        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(new DefaultCredentialProvider(accessKey, secretKey))
                .region(region)
                .build();
        return ossClient;
    }
}
