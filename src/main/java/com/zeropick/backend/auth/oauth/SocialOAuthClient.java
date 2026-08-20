package com.zeropick.backend.auth.oauth;

import com.zeropick.backend.user.enums.AuthProvider;

public interface SocialOAuthClient {
    AuthProvider provider();
    SocialUserInfo getUserInfo(String authCode);
}
