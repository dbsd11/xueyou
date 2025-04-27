package com.wxm.wuyou.controller;

import com.wxm.wuyou.controller.AuthController;
import com.wxm.wuyou.service.CodeService;
import com.wxm.wuyou.service.CryptoService;
import com.wxm.wuyou.utils.FingerprintUtil;
import org.junit.jupiter.api.BeforeEach; // 替换为 JUnit 5 的注解
import org.junit.jupiter.api.Test; // 替换为 JUnit 5 的注解
import org.junit.jupiter.api.extension.ExtendWith; // 使用 JUnit 5 的扩展机制
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension; // JUnit 5 的 Mockito 扩展
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class) // 使用 JUnit 5 的 Mockito 扩展
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CodeService codeService;

    @Mock
    private CryptoService cryptoService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthController authController;

    @BeforeEach // 替换为 JUnit 5 的注解
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void sendCode_EmptyPhone_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/sendCode")
                .param("phone", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("手机号不能为空"));
    }

    @Test
    public void sendCode_CodeGenerationFails_ReturnsInternalServerError() throws Exception {
        when(codeService.generateCode("1234567890")).thenReturn(null);

        mockMvc.perform(post("/sendCode")
                .param("phone", "1234567890"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("验证码生成失败"));

        verify(codeService, times(1)).generateCode("1234567890");
    }

    // 其他测试方法保持不变...
}
