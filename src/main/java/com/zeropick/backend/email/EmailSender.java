package com.zeropick.backend.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
public class EmailSender {

    private static final long CODE_EXPIRATION_MINUTES = 5;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String mailgunDomain;
    private final String mailgunApiKey;

    public EmailSender(@Value("${mailgun.domain}") String mailgunDomain,
                       @Value("${mailgun.api-key}") String mailgunApiKey) {
        this.mailgunDomain = mailgunDomain;
        this.mailgunApiKey = mailgunApiKey;
    }

    @Async
    public void send(String to, String code) {
        try {
            String url = "https://api.mailgun.net/v3/" + mailgunDomain + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String auth = "api:" + mailgunApiKey;
            headers.set("Authorization", "Basic "
                    + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8)));

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("from", "ZeroPick <postmaster@" + mailgunDomain + ">");
            body.add("to", to);
            body.add("subject", "[ZeroPick] 이메일 인증코드");
            body.add("html", "<p>인증코드: <strong>" + code + "</strong></p><p>이 코드는 "
                    + CODE_EXPIRATION_MINUTES + "분 동안 유효합니다.</p>");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, String.class);

            log.info("[인증코드 발송 완료] to={}", to);
        } catch (Exception e) {
            log.error("[인증코드 발송 실패] to={}, error={}", to, e.getMessage());
        }
    }
}