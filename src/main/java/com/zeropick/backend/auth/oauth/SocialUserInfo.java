package com.zeropick.backend.auth.oauth;

public record SocialUserInfo(
        String providerId, String email, String nickname
) {
}
