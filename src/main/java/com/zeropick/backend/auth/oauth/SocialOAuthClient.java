package com.zeropick.backend.auth.oauth;

import com.zeropick.backend.user.AuthProvider;

public interface SocialOAuthClient {
    AuthProvider provider();
    SocialUserInfo getUserInfo(String authCode);
}
