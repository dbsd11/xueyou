package group.bison.xueyou.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = {
    "aliyun.sms.accessKeyId=testAccessKeyId",
    "aliyun.sms.accessKeySecret=testAccessKeySecret",
    "aliyun.sms.signName=testSignName",
    "aliyun.sms.templateCode=testTemplateCode"
})
public class AliyunSmsConfigPropertiesTest {

    @Autowired
    private AliyunSmsConfigProperties aliyunSmsConfigProperties;

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void testAliyunSmsConfigProperties_AreSetCorrectly() {
        assertEquals("testAccessKeyId", aliyunSmsConfigProperties.getAccessKeyId());
        assertEquals("testAccessKeySecret", aliyunSmsConfigProperties.getAccessKeySecret());
        assertEquals("testSignName", aliyunSmsConfigProperties.getSignName());
        assertEquals("testTemplateCode", aliyunSmsConfigProperties.getTemplateCode());
    }
}
