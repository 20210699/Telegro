package com.telegro.telegro.global.common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CookieUtil {

    // 쿠키를 가져오는 메서드
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> name.equals(cookie.getName()))
                    .findFirst();
        }
        return Optional.empty();
    }

    // 쿠키를 설정하는 메서드
    public static void createCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge); // 쿠키 유효기간 설정
        cookie.setPath("/"); // 모든 경로에서 쿠키 사용 가능
        cookie.setAttribute("SameSite","None");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    // 쿠키를 삭제하는 메서드
    public static void deleteCookie(HttpServletResponse response, String name) {
        createCookie(response, name, "", 0);
    }
}

