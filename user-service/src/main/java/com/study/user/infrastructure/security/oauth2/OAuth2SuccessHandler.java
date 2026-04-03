package com.study.user.infrastructure.security.oauth2;

import com.study.user.infrastructure.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Long userId = oAuth2User.getAttribute("id");
        String email = oAuth2User.getAttribute("email");

        String accessToken = jwtTokenProvider.createAccessToken(userId, email, "USER");
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"accessToken\":\"%s\",\"refreshToken\":\"%s\"}", accessToken, refreshToken));
    }
}
