package group.bison.xueyou.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CookieUtilTest {

    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    public void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    public void createCookie_NullName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            CookieUtil.createCookie(null, "value", 3600, "/", false, false)
        );
    }

    @Test
    public void createCookie_InvalidName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            CookieUtil.createCookie("invalid;name", "value", 3600, "/", false, false)
        );
    }

    @Test
    public void createCookie_NullValue_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            CookieUtil.createCookie("name", null, 3600, "/", false, false)
        );
    }

    @Test
    public void createCookie_InvalidValue_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            CookieUtil.createCookie("name", "invalid=value", 3600, "/", false, false)
        );
    }

    @Test
    public void createCookie_ValidInput_ReturnsCookie() {
        Cookie cookie = CookieUtil.createCookie("name", "value", 3600, "/path", true, true);
        assertEquals("name", cookie.getName());
        assertEquals("value", cookie.getValue());
        assertEquals(3600, cookie.getMaxAge());
        assertEquals("/path", cookie.getPath());
        assertTrue(cookie.getSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    public void getCookie_NullRequest_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            CookieUtil.getCookie(null, "name")
        );
    }

    @Test
    public void getCookie_NullName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            CookieUtil.getCookie(request, null)
        );
    }

    @Test
    public void getCookie_NoCookies_ReturnsNull() {
        when(request.getCookies()).thenReturn(null);
        assertNull(CookieUtil.getCookie(request, "name"));
    }

    @Test
    public void getCookie_NoMatchingCookie_ReturnsNull() {
        Cookie[] cookies = {new Cookie("otherName", "value")};
        when(request.getCookies()).thenReturn(cookies);
        assertNull(CookieUtil.getCookie(request, "name"));
    }

    @Test
    public void getCookie_MatchingCookie_ReturnsCookie() {
        Cookie expectedCookie = new Cookie("name", "value");
        Cookie[] cookies = {expectedCookie};
        when(request.getCookies()).thenReturn(cookies);
        assertSame(expectedCookie, CookieUtil.getCookie(request, "name"));
    }

    @Test
    public void deleteCookie_NullResponse_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            CookieUtil.deleteCookie(null, "name", "/")
        );
    }

    @Test
    public void deleteCookie_NullName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            CookieUtil.deleteCookie(response, null, "/")
        );
    }

    @Test
    public void deleteCookie_ValidInput_AddsCookieToResponse() {
        CookieUtil.deleteCookie(response, "name", "/path");
        verify(response).addCookie(argThat(cookie ->
                "name".equals(cookie.getName()) &&
                        "".equals(cookie.getValue()) &&
                        0 == cookie.getMaxAge() &&
                        "/path".equals(cookie.getPath())
        ));
    }

    @Test
    public void containsInvalidChars_InvalidString_ReturnsTrue() {
        assertTrue(CookieUtil.containsInvalidChars("invalid;name"));
    }

    @Test
    public void containsInvalidChars_ValidString_ReturnsFalse() {
        assertFalse(CookieUtil.containsInvalidChars("validName"));
    }
}
