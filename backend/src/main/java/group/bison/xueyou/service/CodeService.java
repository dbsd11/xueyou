package group.bison.xueyou.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class CodeService {
    private static final int CODE_LENGTH = 6; // 验证码长度
    private static final long CODE_EXPIRATION_MINUTES = 5; // 验证码有效期（分钟）
    private static final Random RANDOM = new Random(); // 线程安全的单例 Random 实例

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 生成并存储验证码
     *
     * @param phone 用户手机号
     * @return 生成的验证码
     */
    public String generateCode(String phone) {
    // 检查手机号是否为空或为null
        if (phone == null || phone.isEmpty()) {
        // 如果手机号为空或为null，抛出非法参数异常
            throw new IllegalArgumentException("Phone number cannot be null or empty.");
        }

    // 调用generateRandomCode方法生成随机验证码
        String code = generateRandomCode();
        try {
        // 使用redisTemplate将生成的验证码存储到Redis中
        // key为手机号，value为验证码，过期时间为CODE_EXPIRATION_MINUTES分钟
            redisTemplate.opsForValue().set(phone, code, CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            // 记录异常日志
            System.err.println("Error occurred while storing code in Redis: " + e.getMessage());
            return null; // 返回 null 表示生成失败
        }
        return code;
    }

    /**
     * 验证验证码是否正确
     *
     * @param phone 用户手机号
     * @param code  用户输入的验证码
     * @return 验证结果
     */
    public boolean validateCode(String phone, String code) {
    // 检查手机号和验证码是否为空或null
        if (phone == null || phone.isEmpty() || code == null || code.isEmpty()) {
        // 如果手机号或验证码为空，抛出非法参数异常
            throw new IllegalArgumentException("Phone and code cannot be null or empty.");
        }

        try {
        // 从Redis中获取存储的验证码
            String storedCode = redisTemplate.opsForValue().get(phone);
        // 检查存储的验证码是否不为null且与用户输入的验证码相等
            if (storedCode != null && storedCode.equals(code)) {
            // 如果验证码正确，删除Redis中对应的验证码
                redisTemplate.delete(phone); // 删除已验证的验证码
            // 返回验证成功
                return true;
            }
        } catch (Exception e) {
            // 记录异常日志
            System.err.println("Error occurred while validating code: " + e.getMessage());
        }
        return false;
    }

    /**
     * 生成指定长度的随机数字验证码
     * @return 随机验证码
     */
    private String generateRandomCode() {
    // 创建一个StringBuilder对象，用于构建随机验证码字符串，长度为CODE_LENGTH
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
    // 循环CODE_LENGTH次，每次生成一个随机数字并追加到StringBuilder中
        for (int i = 0; i < CODE_LENGTH; i++) {
        // 使用RANDOM对象的nextInt方法生成一个0到9之间的随机整数，并追加到StringBuilder中
            sb.append(RANDOM.nextInt(10)); // 生成 0-9 的随机数字
        }
    // 将StringBuilder转换为字符串并返回
        return sb.toString();
    }
}
