package com.wxm.wuyou.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtil {

    /**
     * 创建一个Cookie并设置其属性
     *
     * @param name     Cookie名称，必须符合Cookie命名规范
     * @param value    Cookie值，必须符合Cookie值规范
     * @param maxAge   Cookie最大存活时间（秒），负数表示会话期间有效
     * @param path     Cookie路径，默认为"/"
     * @param secure   是否仅通过HTTPS传输
     * @param httpOnly 是否仅通过HTTP访问，防止JavaScript访问
     * @return 创建的Cookie对象
     * @throws IllegalArgumentException 如果name或value包含非法字符
     */
    public static Cookie createCookie(String name, String value, int maxAge, String path, boolean secure, boolean httpOnly) {
    // 检查Cookie名称是否为空或包含非法字符
        if (name == null || name.isEmpty() || containsInvalidChars(name)) {
            throw new IllegalArgumentException("Invalid cookie name: " + name);
        }
    // 检查Cookie值是否为空或包含非法字符
        if (value == null || containsInvalidChars(value)) {
            throw new IllegalArgumentException("Invalid cookie value: " + value);
        }
    // 如果路径为空或为空字符串，则默认设置为"/"
        if (path == null || path.isEmpty()) {
            path = "/";
        }

    // 创建一个新的Cookie对象，并设置其名称和值
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge); // 负数表示会话期间有效
        cookie.setPath(path);
        cookie.setSecure(secure);
        cookie.setHttpOnly(httpOnly);
        return cookie;
    }

    /**
     * 从请求中获取指定名称的Cookie
     * @param request 请求对象，不能为空
     * @param name    Cookie名称，不能为空
     * @return 找到的Cookie对象，未找到则返回null
     * @throws IllegalArgumentException 如果request或name为null
     */
    public static Cookie getCookie(HttpServletRequest request, String name) {
    // 检查请求对象是否为空，如果为空则抛出IllegalArgumentException异常
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
    // 检查Cookie名称是否为空或为空字符串，如果为空则抛出IllegalArgumentException异常
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Cookie name cannot be null or empty");
        }

    // 从请求对象中获取所有的Cookie数组
        Cookie[] cookies = request.getCookies();
    // 检查Cookie数组是否为空，如果不为空则遍历数组
        if (cookies != null) {
        // 遍历所有的Cookie
            for (Cookie cookie : cookies) {
            // 检查当前Cookie的名称是否与指定的名称相等
                if (name.equals(cookie.getName())) {
                // 如果相等，则返回当前Cookie对象
                    return cookie;
                }
            }
        }
    // 如果遍历完所有Cookie后仍未找到指定名称的Cookie，则返回null
        return null;
    }

    /**
     * 删除指定名称的Cookie
     *
     * @param response 响应对象，不能为空
     * @param name     Cookie名称，不能为空
     * @param path     Cookie路径，默认为"/"
     * @throws IllegalArgumentException 如果response或name为null
     */
    public static void deleteCookie(HttpServletResponse response, String name, String path) {
    // 检查响应对象是否为空，如果为空则抛出IllegalArgumentException异常
        if (response == null) {
            throw new IllegalArgumentException("Response cannot be null");
        }
    // 检查Cookie名称是否为空或为空字符串，如果为空则抛出IllegalArgumentException异常
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Cookie name cannot be null or empty");
        }
    // 检查Cookie路径是否为空或为空字符串，如果为空则默认设置为"/"
        if (path == null || path.isEmpty()) {
            path = "/";
        }

    // 创建一个新的Cookie对象，名称为传入的name，值为空字符串
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0); // 设置为0表示立即删除
        cookie.setPath(path);
        response.addCookie(cookie);
    }

    /**
     * 检查字符串是否包含非法字符（如分号、逗号等）
     *
     * @param input 待检查的字符串
     * @return 如果包含非法字符返回true，否则返回false
     */
    protected static boolean containsInvalidChars(String input) {
    // 检查输入字符串是否为null，如果是null则直接返回false，表示不包含非法字符
        if (input == null) {
            return false;
        }
    // 使用正则表达式匹配输入字符串，检查是否包含分号、等号、逗号或空白字符
    // 正则表达式解释：
    // .* 表示任意字符出现任意次数
    // [;=,\\s] 表示匹配分号、等号、逗号或空白字符中的任意一个
    // .* 表示任意字符出现任意次数
    // 如果匹配成功，返回true，表示包含非法字符；否则返回false
        return input.matches(".*[;=,\\s].*"); // 禁止分号、等号、逗号和空白字符
    }
}
