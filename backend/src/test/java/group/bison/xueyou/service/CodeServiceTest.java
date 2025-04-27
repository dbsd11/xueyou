package group.bison.xueyou.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // 使用 MockitoExtension 替代 MockitoJUnitRunner
public class CodeServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private CodeService codeService;

    private ValueOperations<String, String> valueOperations;

    @BeforeEach // 替换 @Before 为 @BeforeEach
    public void setUp() {
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void generateCode_NullOrEmptyPhone_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> codeService.generateCode(null));
    }

    @Test
    public void generateCode_ValidPhone_ReturnsCode() {
        String phone = "1234567890";
        String code = codeService.generateCode(phone);
        assertNotNull(code);
        assertEquals(6, code.length());
        verify(valueOperations).set(eq(phone), eq(code), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    public void generateCode_StorageException_ReturnsNull() {
        String phone = "1234567890";
        doThrow(new RuntimeException("Redis error")).when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        assertNull(codeService.generateCode(phone));
    }

    @Test
    public void validateCode_NullOrEmptyPhoneOrCode_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> codeService.validateCode(null, "123456"));
    }

    @Test
    public void validateCode_ValidCode_ReturnsTrue() {
        String phone = "1234567890";
        String code = "123456";
        when(valueOperations.get(phone)).thenReturn(code);
        assertTrue(codeService.validateCode(phone, code));
        verify(redisTemplate).delete(phone); // 修改为验证 redisTemplate 的 delete 方法
    }

    @Test
    public void validateCode_InvalidCode_ReturnsFalse() {
        String phone = "1234567890";
        String code = "123456";
        when(valueOperations.get(phone)).thenReturn("654321");
        assertFalse(codeService.validateCode(phone, code));
    }

    @Test
    public void validateCode_ValidationException_ReturnsFalse() {
        String phone = "1234567890";
        String code = "123456";
        when(valueOperations.get(phone)).thenThrow(new RuntimeException("Redis error"));
        assertFalse(codeService.validateCode(phone, code));
    }
}
