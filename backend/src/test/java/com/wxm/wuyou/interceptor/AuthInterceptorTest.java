package com.wxm.wuyou.interceptor;

import com.wxm.wuyou.service.CryptoService;
import com.wxm.wuyou.utils.FingerprintUtil;
import org.junit.jupiter.api.BeforeEach; // 替换为 JUnit 5 的注解
import org.junit.jupiter.api.Test; // 替换为 JUnit 5 的注解
import org.junit.jupiter.api.extension.ExtendWith; // 替换为 JUnit 5 的扩展机制
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension; // 使用 JUnit 5 的 Mockito 扩展
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class) // 使用 JUnit 5 的 Mockito 扩展
public class AuthInterceptorTest {

    private MockMvc mockMvc;

    @Mock
    private CryptoService cryptoService;

    @Mock
    private FingerprintUtil fingerprintUtil;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    @BeforeEach // 替换为 JUnit 5 的注解
    public void setup() {
        MockitoAnnotations.openMocks(this); // JUnit 5 推荐使用 openMocks
        mockMvc = MockMvcBuilders.standaloneSetup(authInterceptor).build();
    }

    @Test
    public void preHandle_NoCookies_Returns401() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getCookies()).thenReturn(null);

        mockMvc.perform(get("/test"))
                .andExpect(status().isUnauthorized());
    }

    // 其他测试方法保持不变...
}
