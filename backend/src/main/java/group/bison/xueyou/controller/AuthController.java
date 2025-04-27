package group.bison.xueyou.controller;

import group.bison.xueyou.service.CodeService;
import group.bison.xueyou.service.CryptoService;
import group.bison.xueyou.utils.PhoneUtil;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;


// 验证码相关接口
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private CodeService codeService; // 注入验证码服务
    @Autowired
    private CryptoService cryptoService; // 注入加密服务

    /**
     * 发送验证码接口
     */
    @PostMapping("/sendCode")
    public ResponseEntity<String> sendCode(@RequestParam String phone) {
        // 检查手机号是否为空
        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest().body("手机号不能为空");
        }

        try {
            String code = codeService.generateCode(phone); // 生成并存储验证码
            if (code == null || code.isEmpty()) {
                return ResponseEntity.status(500).body("验证码生成失败");
            }
            sendVerificationCodeDingDing(phone, code);
            logger.info("验证码已发送至手机号: {}", phone);
            return ResponseEntity.ok("验证码已发送");
        } catch (Exception e) {
            logger.error("验证码发送失败，手机号: {}, 错误信息: {}", phone, e.getMessage(), e);
            return ResponseEntity.status(500).body("验证码发送失败");
        }
    }

    /**
     * 验证验证码接口
     */
    @PostMapping("/verify")
    public ResponseEntity<String> verify(
            @RequestParam String phone, // 从请求参数中获取手机号
            @RequestParam String code, // 从请求参数中获取验证码
            HttpServletRequest request, // HTTP请求对象
            HttpServletResponse response) { // HTTP响应对象

    // 检查手机号和验证码是否为空
        if (phone == null || phone.isEmpty() || code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("手机号或验证码不能为空"); // 返回400错误，提示手机号或验证码不能为空
        }

        try {
        // 调用codeService验证手机号和验证码
            if (!codeService.validateCode(phone, code)) {
                logger.warn("验证码验证失败，手机号: {}", phone); // 记录警告日志
                return ResponseEntity.status(401).body("验证码错误"); // 返回401错误，提示验证码错误
            }

            String fp = request.getHeader("fp");

            // 假设 FingerprintUtil.generate 不会返回 null，仅检查 isEmpty()
            if (fp == null || fp.isEmpty()) {
                logger.error("指纹生成失败，手机号: {}", phone); // 记录错误日志
                return ResponseEntity.status(500).body("系统错误，请稍后再试"); // 返回500错误，提示系统错误
            }

        // 使用cryptoService加密手机号和指纹
            String cookieValue = cryptoService.encrypt(phone, fp);
            if (cookieValue == null || cookieValue.isEmpty()) {
                logger.error("加密失败，手机号: {}", phone); // 记录错误日志
                return ResponseEntity.status(500).body("系统错误，请稍后再试"); // 返回500错误，提示系统错误
            }

        // 创建Cookie对象
            Cookie cookie = new Cookie("token", cookieValue);
            URL originURL = new URL(request.getHeader("origin"));
            cookie.setDomain(originURL.getHost());
            cookie.setHttpOnly(false); // 不用限制Cookie为HttpOnly，不用防止客户端脚本访问
            cookie.setSecure(false); // 不用限制HTTPS访问
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24); // 设置Cookie有效期为1天
            response.addCookie(cookie);

            logger.info("验证成功，手机号: {}", phone);
            return ResponseEntity.ok("验证成功");
        } catch (Exception e) {
            logger.error("验证过程中发生错误，手机号: {}, 错误信息: {}", phone, e.getMessage(), e);
            return ResponseEntity.status(500).body("系统错误，请稍后再试");
        }
    }

    protected void sendVerificationCodeDingDing(String phoneNumber, String code) {
        try {
            HttpPost httpPost = new HttpPost("https://oapi.dingtalk.com/robot/send?access_token=13615ba6938f27807d35b613d81c5a4ec9566d457439976aed6e5430076d8eeb");
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            String messageContent = "[WUYOU] login with code. phone: " + PhoneUtil.encrypt(phoneNumber) + " code: " + code;
            httpPost.setEntity(new StringEntity(String.format("{\"msgtype\":\"text\", \"text\":{\"content\": \"%s\"}}", messageContent)));
            HttpClient httpClient = HttpClientBuilder.create().build();
            httpClient.execute(httpPost);
            logger.info("dingding验证码发送成功");
        } catch (Exception e) {
            logger.error("dingding验证码发送失败， " + e.getMessage());
        }
    }
}

