package com.zeropick.backend.global.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.time.Duration;

@Component
public class CookieUtil {
    public static final String EMAIL_VERIFY_SESSION_COOKIE = "email_verify_session";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final boolean secure;
    private final String sameSite;
    private final String domain;

    public CookieUtil(
            @Value("${app.cookie.secure:true}") boolean secure,
            @Value("${app.cookie.same-site:None}") String sameSite,
            @Value("${app.cookie.domain:}") String domain
    ) {
        this.secure = secure; this.sameSite = sameSite; this.domain = domain;
    }

    public ResponseCookie create(String name, String value, String path, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true).secure(secure).sameSite(sameSite).path(path).maxAge(maxAge);
        if (StringUtils.hasText(domain)) builder.domain(domain);
        return builder.build();
    }

    public ResponseCookie delete(String name, String path) {
        return create(name, "", path, Duration.ZERO);
    }
}