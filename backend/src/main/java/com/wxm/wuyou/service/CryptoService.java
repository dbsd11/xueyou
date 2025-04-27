package com.wxm.wuyou.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;

// 加密服务
@Service
public class CryptoService {

    @Value("${crypto.secretKey:default_secret_key}")
    private String secretKey;

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CryptoService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }



// 定义一个公共的方法，用于设置密钥
    public void setSecretKey(String secretKey) {
    // 将传入的密钥赋值给当前对象的secretKey属性
        this.secretKey = secretKey;
    }

// 定义一个公共的方法，用于获取密钥
    public String getSecretKey() {
    // 返回当前对象的secretKey属性值
        return secretKey;
    }
    @Value("${cookie.secret}") // 从配置读取密钥

    /**
     * 加密方法
     *
     * @param phone 用户手机号
     * @param fp    用户指纹
     * @return 加密后的字符串
     * @throws Exception 如果加密过程中发生错误
     */
    public String encrypt(String phone, String fp) throws Exception {
        // 检查输入参数和密钥是否为空
        if (phone == null || fp == null || secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("输入参数或密钥不能为空");
        }

        // 将手机号和指纹拼接成原始数据
        String rawData = phone + "|" + fp;

        // 确保密钥长度符合 AES 要求
        byte[] keyBytes = ensureValidKeyLength(secretKey.getBytes());

        // 创建 AES 加密实例
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
        // 加密原始数据并转换为 Base64 编码字符串
        String encryptedData = Base64.getEncoder().encodeToString(cipher.doFinal(rawData.getBytes()));

        // 将加密后的数据存储到 Redis
        redisTemplate.opsForValue().set(phone, encryptedData);

        return encryptedData;
    }

    /**
     * 解密方法
     *
     * @param cookie 加密后的字符串
     * @return 解密后的数组 [手机号, 指纹]
     * @throws Exception 如果解密过程中发生错误
     */
    public String[] decrypt(String cookie) throws Exception {
        // 检查输入参数和密钥是否为空
        if (cookie == null || secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("输入参数或密钥不能为空");
        }

        // 确保密钥长度符合 AES 要求
        byte[] keyBytes = ensureValidKeyLength(secretKey.getBytes());

        // 创建 AES 解密实例
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
        // 解密 Base64 编码的字符串并转换为原始数据
        String rawData = new String(cipher.doFinal(Base64.getDecoder().decode(cookie)));
        // 将原始数据按 '|' 分割成手机号和指纹
        return rawData.split("\\|");
    }

    /**
     * 确保密钥长度符合 AES 要求
     *
     * @param keyBytes 原始密钥字节数组
     * @return 符合 AES 要求的密钥字节数组
     */
    private byte[] ensureValidKeyLength(byte[] keyBytes) {
        int validKeyLength = 32; // 默认使用 32 字节
        if (keyBytes.length < validKeyLength) {
            // 如果密钥长度不足，填充为指定长度
            byte[] paddedKey = new byte[validKeyLength];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            return paddedKey;
        } else if (keyBytes.length > validKeyLength) {
            // 如果密钥长度过长，截断为指定长度
            return Arrays.copyOf(keyBytes, validKeyLength);
        }
        return keyBytes;
    }

}
