package com.wxm.wuyou.interceptor;

import com.wxm.wuyou.config.WebConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class WebConfigTest {

    @InjectMocks
    private WebConfig webConfig;

    @Mock
    private AuthInterceptor authInterceptor;

    @Mock
    private Environment environment;

    @Mock
    private InterceptorRegistry registry;

    @BeforeEach
    public void setUp() {
        // 设置环境属性的默认值
        when(environment.getProperty("web.interceptor.includePatterns")).thenReturn("/api/**");
        when(environment.getProperty("web.interceptor.excludePatterns")).thenReturn("/api/sendCode,/api/verify,/api/fingerprint");
    }

    @Test
    public void addInterceptors_AuthInterceptorIsNull_ThrowsIllegalStateException() {
        webConfig = new WebConfig(); // 未注入authInterceptor
        assertThrows(IllegalStateException.class, () -> webConfig.addInterceptors(registry));
    }

    @Test
    public void addInterceptors_AuthInterceptorIsNotNull_RegistersInterceptor() {
        webConfig.addInterceptors(registry);
        Mockito.verify(registry).addInterceptor(authInterceptor);
    }

    @Test
    public void addInterceptors_EnvironmentPropertiesNotSet_UsesDefaults() {
        webConfig.addInterceptors(registry);
        Mockito.verify(registry).addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/sendCode", "/api/verify", "/api/fingerprint");
    }

    @Test
    public void addInterceptors_EnvironmentPropertiesSet_UsesCustomValues() {
        when(environment.getProperty("web.interceptor.includePatterns")).thenReturn("/custom/**");
        when(environment.getProperty("web.interceptor.excludePatterns")).thenReturn("/custom/exclude1,/custom/exclude2");

        webConfig.addInterceptors(registry);
        Mockito.verify(registry).addInterceptor(authInterceptor)
                .addPathPatterns("/custom/**")
                .excludePathPatterns("/custom/exclude1", "/custom/exclude2");
    }
}
