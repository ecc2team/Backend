package com.zeropick.backend.auth.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zeropick.backend.global.exception.UnauthorizedException;
import com.zeropick.backend.user.AuthProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class GoogleOAuthClient implements SocialOAuthClient {

    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GoogleOAuthClient(
            RestClient.Builder restClientBuilder,
            @Value("${oauth.google.client-id}") String clientId,
            @Value("${oauth.google.client-secret}") String clientSecret,
            @Value("${oauth.google.redirect-uri}") String redirectUri
    ) {
        this.restClient = restClientBuilder.build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.GOOGLE;
    }

    @Override
    public SocialUserInfo getUserInfo(String authCode) {
        String googleAccessToken = requestAccessToken(authCode);
        return requestUserInfo(googleAccessToken);
    }

    private String requestAccessToken(String authCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", authCode);

        try {
            GoogleTokenResponse response = restClient.post()
                    .uri(TOKEN_URI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(GoogleTokenResponse.class);

            if (response == null || response.accessToken() == null) {
                throw new UnauthorizedException("구글 인증에 실패했습니다.");
            }
            return response.accessToken();
        } catch (RestClientException e) {
            throw new UnauthorizedException("구글 인증 코드가 유효하지 않습니다.");
        }
    }

    private SocialUserInfo requestUserInfo(String googleAccessToken) {
        try {
            GoogleUserResponse response = restClient.get()
                    .uri(USER_INFO_URI)
                    .header("Authorization", "Bearer " + googleAccessToken)
                    .retrieve()
                    .body(GoogleUserResponse.class);

            if (response == null || response.email() == null) {
                throw new UnauthorizedException("구글 계정에서 이메일 정보를 가져올 수 없습니다.");
            }

            return new SocialUserInfo(response.sub(), response.email(), response.name());
        } catch (RestClientException e) {
            throw new UnauthorizedException("구글 사용자 정보 조회에 실패했습니다.");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleUserResponse(String sub, String email, String name) {
    }
}