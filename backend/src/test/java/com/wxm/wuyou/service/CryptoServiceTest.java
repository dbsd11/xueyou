package com.wxm.wuyou.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class CryptoServiceTest {

    @InjectMocks
    private CryptoService cryptoService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    private AutoCloseable mocks;

    @BeforeEach
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        cryptoService.setSecretKey("1234567890123456"); // 16字节密钥
    }

    @Test
    public void encrypt_ValidInputs_ReturnsEncryptedString() throws Exception {
        String phone = "1234567890";
        String fp = "fingerprint";

        String encryptedData = cryptoService.encrypt(phone, fp);

        // 验证 Redis 存储
        verify(redisTemplate).opsForValue().set(phone, encryptedData);

        // 验证加密数据
        String[] decryptedData = cryptoService.decrypt(encryptedData);
        assertEquals(phone, decryptedData[0]);
        assertEquals(fp, decryptedData[1]);
    }

    @Test
    public void encrypt_NullPhone_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> cryptoService.encrypt(null, "fingerprint"));
    }

    @Test
    public void encrypt_NullFingerprint_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> cryptoService.encrypt("1234567890", null));
    }

    @Test
    public void encrypt_NullSecretKey_ThrowsException() {
        cryptoService.setSecretKey(null);
        assertThrows(IllegalArgumentException.class, () -> cryptoService.encrypt("1234567890", "fingerprint"));
    }

    @Test
    public void encrypt_EmptySecretKey_ThrowsException() {
        cryptoService.setSecretKey("");
        assertThrows(IllegalArgumentException.class, () -> cryptoService.encrypt("1234567890", "fingerprint"));
    }

    @Test
    public void encrypt_InvalidKeyLength_ThrowsException() {
        cryptoService.setSecretKey("123456789012345"); // 15字节密钥
        assertThrows(IllegalArgumentException.class, () -> cryptoService.encrypt("1234567890", "fingerprint"));
    }

    @Test
    public void decrypt_ValidCookie_ReturnsDecryptedData() throws Exception {
        String phone = "1234567890";
        String fp = "fingerprint";
        String rawData = phone + "|" + fp;
        byte[] keyBytes = cryptoService.getSecretKey().getBytes();
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
        String encryptedData = Base64.getEncoder().encodeToString(cipher.doFinal(rawData.getBytes()));

        String[] decryptedData = cryptoService.decrypt(encryptedData);

        assertEquals(phone, decryptedData[0]);
        assertEquals(fp, decryptedData[1]);
    }

    @Test
    public void decrypt_NullCookie_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> cryptoService.decrypt(null));
    }

    @BeforeEach
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }
}
