package com.wxm.wuyou.controller;

import com.wxm.wuyou.controller.VerificationController;
import com.wxm.wuyou.utils.VerificationCodeUtil;
import org.junit.jupiter.api.BeforeEach; // 使用 JUnit 5 的注解
import org.junit.jupiter.api.Test; // 使用 JUnit 5 的注解
import org.junit.jupiter.api.extension.ExtendWith; // 使用 JUnit 5 的扩展机制
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension; // 使用 Mockito 的 JUnit 5 扩展
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class) // 使用 Mockito 的 JUnit 5 扩展
public class VerificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VerificationCodeUtil verificationCodeUtil;

    @Mock
    private Logger logger;

    @InjectMocks
    private VerificationController verificationController;

    @BeforeEach // 替换为 JUnit 5 的注解
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(verificationController).build();
    }

    @Test
    public void sendVerificationCode_ValidRequest_ReturnsSuccess() throws Exception {
        doNothing().when(verificationCodeUtil).generateAndSendCode("12345678901");

        mockMvc.perform(post("/api/verification/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phoneNumber\":\"12345678901\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("验证码已发送"));

        verify(verificationCodeUtil, times(1)).generateAndSendCode("12345678901");
        verify(logger, times(1)).info("验证码已发送至手机号: {}", "12345678901");
    }

    @Test
    public void sendVerificationCode_InvalidRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/verification/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phoneNumber\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("请求体不能为空"));

        verify(verificationCodeUtil, times(0)).generateAndSendCode(anyString());
        verify(logger, times(1)).warn("无效的手机号码: {}", "123");
    }

    @Test
    public void sendVerificationCode_ExceptionThrown_ReturnsServerError() throws Exception {
        doThrow(new RuntimeException("Test exception")).when(verificationCodeUtil).generateAndSendCode("12345678901");

        mockMvc.perform(post("/api/verification/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phoneNumber\":\"12345678901\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("系统错误，请稍后再试"));

        verify(verificationCodeUtil, times(1)).generateAndSendCode("12345678901");
        verify(logger, times(1)).error("发送验证码失败: {}", "Test exception");
    }

    @Test
    public void validateVerificationCode_ValidRequest_ReturnsSuccess() throws Exception {
        when(verificationCodeUtil.validateCode("12345678901", "123456")).thenReturn(true);

        mockMvc.perform(post("/api/verification/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phoneNumber\":\"12345678901\",\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("验证码验证成功"));

        verify(verificationCodeUtil, times(1)).validateCode("12345678901", "123456");
        verify(logger, times(1)).info("验证码验证成功，手机号: {}", "12345678901");
    }

    @Test
    public void validateVerificationCode_InvalidRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/verification/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phoneNumber\":\"123\",\"code\":\"123456\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("请求体不能为空"));

        verify(verificationCodeUtil, times(0)).validateCode(anyString(), anyString());
        verify(logger, times(1)).warn("无效的手机号码: {}", "123");
    }

    @Test
    public void validateVerificationCode_InvalidCode_ReturnsBadRequest() throws Exception {
        when(verificationCodeUtil.validateCode("12345678901", "123456")).thenReturn(false);

        mockMvc.perform(post("/api/verification/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phoneNumber\":\"12345678901\",\"code\":\"123456\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("验证码验证失败"));

        verify(verificationCodeUtil, times(1)).validateCode("12345678901", "123456");
        verify(logger, times(1)).warn("验证码验证失败，手机号: {}", "12345678901");
    }

    @Test
    public void validateVerificationCode_ExceptionThrown_ReturnsServerError() throws Exception {
        doThrow(new RuntimeException("Test exception")).when(verificationCodeUtil).validateCode("12345678901", "123456");

        mockMvc.perform(post("/api/verification/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phoneNumber\":\"12345678901\",\"code\":\"123456\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("系统错误，请稍后再试"));

        verify(verificationCodeUtil, times(1)).validateCode("12345678901", "123456");
        verify(logger, times(1)).error("验证码验证失败: {}", "Test exception");
    }
}
