package group.bison.xueyou.config;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EnableConfigurationProperties(CookieConfigProperties.class)
@TestPropertySource(properties = "cookie.secret=testSecret")
public class CookieConfigPropertiesTest {

    private CookieConfigProperties cookieConfigProperties;

    @BeforeEach
    public void setUp() {
        cookieConfigProperties = new CookieConfigProperties();
    }

    @Test
    public void getSecret_ShouldReturnCorrectSecret() {
        // 设置
        String expectedSecret = "testSecret";
        cookieConfigProperties.setSecret(expectedSecret);

        // 验证
        String actualSecret = cookieConfigProperties.getSecret();
        assertEquals(expectedSecret, actualSecret, "The secret should match the expected value.");
    }

    @Test
    public void setSecret_ShouldSetCorrectSecret() {
        // 设置
        String newSecret = "newSecret";
        cookieConfigProperties.setSecret(newSecret);

        // 验证
        String actualSecret = cookieConfigProperties.getSecret();
        assertEquals(newSecret, actualSecret, "The secret should be set to the new value.");
    }
}
