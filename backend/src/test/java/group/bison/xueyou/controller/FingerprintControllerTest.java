package group.bison.xueyou.controller;

import com.wxm.wuyou.controller.FingerprintController;
import group.bison.xueyou.service.CodeService;
import group.bison.xueyou.service.CryptoService;
import com.wxm.wuyou.utils.FingerprintUtil;
import org.junit.jupiter.api.BeforeEach; // 使用 JUnit 5 的 @BeforeEach
import org.junit.jupiter.api.Test; // 使用 JUnit 5 的 @Test
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith(MockitoExtension.class) // 使用 MockitoExtension 配合 JUnit 5
public class FingerprintControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CodeService codeService;

    @Mock
    private CryptoService cryptoService;

    @InjectMocks
    private FingerprintController fingerprintController;

    @BeforeEach // 替换为 JUnit 5 的 @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(fingerprintController).build();
    }

    @Test
    public void verifyFingerprint_InvalidInput_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/fingerprint")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("参数错误"));
    }

    @Test
    public void verifyFingerprint_InvalidCode_ReturnsUnauthorized() throws Exception {
        when(codeService.validateCode(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/fingerprint")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fingerprint\":\"testFingerprint\",\"phone\":\"1234567890\",\"code\":\"invalidCode\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("验证失败"));
    }

    @Test
    public void verifyFingerprint_FingerprintMismatch_ReturnsUnauthorized() throws Exception {
        when(codeService.validateCode(anyString(), anyString())).thenReturn(true);
        when(FingerprintUtil.generate(any(HttpServletRequest.class))).thenReturn("generatedFingerprint");

        mockMvc.perform(post("/api/fingerprint")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fingerprint\":\"testFingerprint\",\"phone\":\"1234567890\",\"code\":\"validCode\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("验证失败"));
    }

    @Test
    public void verifyFingerprint_EncryptionFailure_ReturnsInternalServerError() throws Exception {
        when(codeService.validateCode(anyString(), anyString())).thenReturn(true);
        when(FingerprintUtil.generate(any(HttpServletRequest.class))).thenReturn("testFingerprint");
        when(cryptoService.encrypt(anyString(), anyString())).thenThrow(new RuntimeException("Encryption failed"));

        mockMvc.perform(post("/api/fingerprint")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fingerprint\":\"testFingerprint\",\"phone\":\"1234567890\",\"code\":\"validCode\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("系统错误"));
    }

    @Test
    public void verifyFingerprint_SuccessfulVerification_ReturnsOk() throws Exception {
        when(codeService.validateCode(anyString(), anyString())).thenReturn(true);
        when(FingerprintUtil.generate(any(HttpServletRequest.class))).thenReturn("testFingerprint");
        when(cryptoService.encrypt(anyString(), anyString())).thenReturn("encryptedValue");

        mockMvc.perform(post("/api/fingerprint")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fingerprint\":\"testFingerprint\",\"phone\":\"1234567890\",\"code\":\"validCode\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("验证成功"));
    }
}
