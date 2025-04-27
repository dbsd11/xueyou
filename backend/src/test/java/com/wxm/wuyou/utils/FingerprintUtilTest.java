package com.wxm.wuyou.utils;

import org.junit.jupiter.api.BeforeEach; // 使用 JUnit 5 的注解
import org.junit.jupiter.api.Test; // 使用 JUnit 5 的注解
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals; // 使用 JUnit 5 的断言
import static org.junit.jupiter.api.Assertions.assertTrue; // 使用 JUnit 5 的断言

public class FingerprintUtilTest {

    private MockHttpServletRequest request;

    @BeforeEach // 替换为 JUnit 5 的注解
    public void setUp() {
        request = new MockHttpServletRequest();
    }

    @Test
    public void generate_AllHeadersPresent_CorrectFingerprint() {
        request.addHeader("User-Agent", "Mozilla/5.0");
        request.addHeader("Accept-Language", "en-US");
        request.addHeader("Time-Zone", "UTC");
        request.addHeader("Screen-Width", "1920");
        request.addHeader("Screen-Height", "1080");
        request.addHeader("Color-Depth", "24");
        request.addHeader("Plugins", "plugin1,plugin2");

        String expectedFingerprint = DigestUtils.md5DigestAsHex(
                "Mozilla/5.0en-USUTC1920108024plugin1,plugin2".getBytes());

        assertEquals(expectedFingerprint, FingerprintUtil.generate(request));
    }

    @Test
    public void generate_SomeHeadersMissing_CorrectFingerprint() {
        request.addHeader("User-Agent", "Mozilla/5.0");
        request.addHeader("Accept-Language", "en-US");
        request.addHeader("Time-Zone", "UTC");

        String expectedFingerprint = DigestUtils.md5DigestAsHex(
                "Mozilla/5.0en-USUTC".getBytes());

        assertEquals(expectedFingerprint, FingerprintUtil.generate(request));
    }

    @Test
    public void generate_SomeHeadersEmpty_CorrectFingerprint() {
        request.addHeader("User-Agent", "");
        request.addHeader("Accept-Language", "en-US");
        request.addHeader("Time-Zone", "UTC");

        String expectedFingerprint = DigestUtils.md5DigestAsHex(
                "en-USUTC".getBytes());

        assertEquals(expectedFingerprint, FingerprintUtil.generate(request));
    }

    @Test
    public void generate_ExceptionOccurs_ReturnsEmptyString() {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader(Mockito.anyString())).thenThrow(new RuntimeException("Test exception"));

        assertEquals("", FingerprintUtil.generate(mockRequest));
    }
}
