package com.wxm.wuyou.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.junit.jupiter.api.BeforeEach; // 替换为 JUnit 5 的注解
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // 使用 MockitoExtension 集成 JUnit 5 和 Mockito
public class VerificationCodeUtilTest {

    private MockMvc mockMvc;

    @InjectMocks
    private VerificationCodeUtil verificationCodeUtil;

    @Mock
    private IAcsClient acsClient;

    @Mock
    private SendSmsResponse sendSmsResponse;

    @BeforeEach // 替换为 JUnit 5 的注解
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(verificationCodeUtil).build();
    }

    @Test
    public void generateAndSendCode_ValidPhoneNumber_CodeGeneratedAndSent() throws ClientException {
        String phoneNumber = "1234567890";
        String expectedCode = "123456"; // 假设生成的代码是 "123456"

        // 模拟 sendVerificationCodeSMS 方法
        doNothing().when(verificationCodeUtil).sendVerificationCodeSMS(phoneNumber, expectedCode);

        // 模拟 sendSmsResponse
        when(sendSmsResponse.getCode()).thenReturn("OK");
        when(acsClient.getAcsResponse(any(SendSmsRequest.class))).thenReturn(sendSmsResponse);

        String actualCode = verificationCodeUtil.generateAndSendCode(phoneNumber);

        assertEquals(expectedCode, actualCode);
        verify(verificationCodeUtil, times(1)).sendVerificationCodeSMS(phoneNumber, expectedCode);
    }

    @Test
    public void validateCode_ValidCode_ReturnsTrue() {
        String phoneNumber = "1234567890";
        String code = "123456";

        // 手动设置 codeMap 以进行测试
        Map<String, VerificationCodeUtil.CodeEntry> codeMap = new HashMap<>();
        codeMap.put(phoneNumber, new VerificationCodeUtil.CodeEntry(code, System.currentTimeMillis() + 5 * 60 * 1000));
        verificationCodeUtil.codeMap = codeMap;

        boolean result = verificationCodeUtil.validateCode(phoneNumber, code);

        assertTrue(result);
        assertFalse(verificationCodeUtil.codeMap.containsKey(phoneNumber));
    }

    @Test
    public void validateCode_InvalidCode_ReturnsFalse() {
        String phoneNumber = "1234567890";
        String code = "123456";

        // 手动设置 codeMap 以进行测试
        Map<String, VerificationCodeUtil.CodeEntry> codeMap = new HashMap<>();
        codeMap.put(phoneNumber, new VerificationCodeUtil.CodeEntry("654321", System.currentTimeMillis() + 5 * 60 * 1000));
        verificationCodeUtil.codeMap = codeMap;

        boolean result = verificationCodeUtil.validateCode(phoneNumber, code);

        assertFalse(result);
        assertTrue(verificationCodeUtil.codeMap.containsKey(phoneNumber));
    }

    @Test
    public void validateCode_ExpiredCode_ReturnsFalse() {
        String phoneNumber = "1234567890";
        String code = "123456";

        // 手动设置 codeMap 以进行测试
        Map<String, VerificationCodeUtil.CodeEntry> codeMap = new HashMap<>();
        codeMap.put(phoneNumber, new VerificationCodeUtil.CodeEntry(code, System.currentTimeMillis() - 1000));
        verificationCodeUtil.codeMap = codeMap;

        boolean result = verificationCodeUtil.validateCode(phoneNumber, code);

        assertFalse(result);
        assertFalse(verificationCodeUtil.codeMap.containsKey(phoneNumber));
    }
}
