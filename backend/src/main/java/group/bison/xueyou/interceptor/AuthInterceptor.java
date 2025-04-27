package group.bison.xueyou.interceptor;

import group.bison.xueyou.service.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    public static final String ATTRIBUTE_PHONE = "phone";

    // 自动注入CryptoService用于解密操作
    @Autowired
    private CryptoService cryptoService;

    @Value("${web.interceptor.excludePatterns:}")
    private List<String> excludePatterns;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 不需要校验token的地址,需要过滤
        boolean ignore = excludePatterns.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, request.getRequestURI()));
        if (ignore) {
            return true;
        }

        // 获取请求中的所有Cookie
        Cookie[] cookies = request.getCookies();

        // 如果没有Cookie，返回401错误，表示未授权
        if (cookies == null || cookies.length == 0) {
            sendErrorResponse(response, 401, "未授权");
            return false;
        }

        // 使用Java 8 Stream API查找名为"FP_AUTH"的Cookie
        Optional<Cookie> authCookie = Arrays.stream(cookies)
                .filter(c -> "token".equals(c.getName()))
                .findFirst();

        // 如果没有找到"FP_AUTH"的Cookie，返回401错误，表示未授权
        if (!authCookie.isPresent()) {
            sendErrorResponse(response, 401, "未授权");
            return false;
        }

        // 获取请求头中的"Fingerprint"值
        String fingerprintHeader = request.getHeader("fp");

        // 如果没有提供"Fingerprint"头，返回401错误，表示指纹未提供
        if (fingerprintHeader == null || fingerprintHeader.isEmpty()) {
            sendErrorResponse(response, 401, "指纹未提供");
            return false;
        }

        try {
            // 解密"FP_AUTH" Cookie的值，获取用户ID和指纹
            String[] parts = cryptoService.decrypt(authCookie.get().getValue());

            // 校验解密结果是否合法
            if (parts == null || parts.length < 2) {
                sendErrorResponse(response, 400, "Cookie无效");
                logger.error("解密结果不完整: {}", authCookie.get().getValue());
                return false;
            }

            // 校验指纹是否匹配
            if (!fingerprintHeader.equals(parts[1])) {
                sendErrorResponse(response, 403, "指纹不匹配");
                logger.warn("指纹校验失败: 请求头指纹={}", fingerprintHeader);
                return false;
            }

            // 将用户ID存入请求属性中，以便后续处理
            request.setAttribute(ATTRIBUTE_PHONE, parts[0]);
        } catch (Exception e) {
            sendErrorResponse(response, 400, "Cookie无效");
            logger.error("解密过程中发生异常: ", e);
            return false;
        }

        return true; // 如果所有检查都通过，返回true，表示请求可以继续处理
    }

    /**
     * 发送错误响应并记录日志
     *
     * @param response 响应对象，用于向客户端发送响应
     * @param statusCode 状态码，表示HTTP响应的状态，如404表示未找到，500表示服务器内部错误等
     * @param message 错误信息，描述具体的错误内容，将随响应发送给客户端
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) {
        try {
        // 尝试发送错误响应，状态码和错误信息将包含在HTTP响应中
            response.sendError(statusCode, message);
        } catch (Exception e) {
        // 如果在发送错误响应过程中发生异常，则记录错误日志
            logger.error("发送错误响应时发生异常: ", e);
        }
    }
}
