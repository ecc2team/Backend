package com.zeropick.backend.global.security;

import com.zeropick.backend.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
        throws ServletException, IOException {

        // 1. 요청 헤더에서 Authorization 추출
        String header = request.getHeader("Authorization");

        // 2. 헤더가 존재하고 "Bearer"로 시작하는 정상 토큰 형태인지 검사
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);  // "Bearer " 7글자 잘라내기

            // 3. 토큰 유효성 검증
            if (jwtUtil.isValid(token)) {
                // 4. 토큰에서 이메일 추출
                String email = jwtUtil.extractEmail(token);

                // 5. DB 유저 확인 및 인증 티켓 생성 후 SecurityContextHolder에 저장
                userRepository.findByEmailAndDeletedAtIsNull(email).ifPresent(user -> {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }
        }

        // 6. 다음 필터 또는 컨트롤러로 요청 진행
        chain.doFilter(request, response);
    }
}
