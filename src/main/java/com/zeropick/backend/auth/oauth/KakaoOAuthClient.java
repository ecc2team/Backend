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
public class KakaoOAuthClient implements SocialOAuthClient {

    private static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public KakaoOAuthClient(
            RestClient.Builder restClientBuilder,
            @Value("${oauth.kakao.client-id}") String clientId,
            @Value("${oauth.kakao.client-secret}") String clientSecret,
            @Value("${oauth.kakao.redirect-uri}") String redirectUri
    ) {
        this.restClient = restClientBuilder.build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.KAKAO;
    }

    @Override
    public SocialUserInfo getUserInfo(String authCode) {
        String kakaoAccessToken = requestAccessToken(authCode);
        return requestUserInfo(kakaoAccessToken);
    }

    // 1. authCode -> 카카오 액세스 토큰
    private String requestAccessToken(String authCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", authCode);

        try {
            KakaoTokenResponse response = restClient.post()
                    .uri(TOKEN_URI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(KakaoTokenResponse.class);

            if (response == null || response.accessToken() == null) {
                throw new UnauthorizedException("카카오 인증에 실패했습니다.");
            }
            return response.accessToken();
        } catch (RestClientException e) {
            throw new UnauthorizedException("카카오 인증 코드가 유효하지 않습니다.");
        }
    }

    // 2. 카카오 액세스 토큰 -> 사용자 정보
    private SocialUserInfo requestUserInfo(String kakaoAccessToken) {
        try {
            KakaoUserResponse response = restClient.get()
                    .uri(USER_INFO_URI)
                    .header("Authorization", "Bearer " + kakaoAccessToken)
                    .retrieve()
                    .body(KakaoUserResponse.class);

            if (response == null || response.kakaoAccount() == null || response.kakaoAccount().email() == null) {
                // 카카오 개발자 콘솔 > 카카오 로그인 > 동의항목에서 email이 "필수 동의"여야 함
                throw new UnauthorizedException("카카오 계정에서 이메일 정보를 가져올 수 없습니다.");
            }

            String nickname = response.kakaoAccount().profile() != null
                    ? response.kakaoAccount().profile().nickname()
                    : null;

            return new SocialUserInfo(String.valueOf(response.id()), response.kakaoAccount().email(), nickname);
        } catch (RestClientException e) {
            throw new UnauthorizedException("카카오 사용자 정보 조회에 실패했습니다.");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoUserResponse(Long id, @JsonProperty("kakao_account") KakaoAccount kakaoAccount) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoAccount(String email, KakaoProfile profile) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoProfile(String nickname) {
    }
}