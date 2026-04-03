package com.study.user.application.service;

import com.study.user.application.dto.TokenResponse;
import com.study.user.domain.entity.AuthProvider;
import com.study.user.domain.entity.Role;
import com.study.user.domain.entity.User;
import com.study.user.domain.repository.UserRepository;
import com.study.user.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestClient restClient;

    @Transactional
    public TokenResponse loginWithGoogle(String accessToken) {
        Map<String, Object> userInfo = restClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String providerId = (String) userInfo.get("sub");

        User user = findOrCreateOAuth2User(email, name, AuthProvider.GOOGLE, providerId);
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse loginWithKakao(String accessToken) {
        Map<String, Object> userInfo = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String email = (String) kakaoAccount.get("email");
        String name = (String) profile.get("nickname");
        String providerId = String.valueOf(userInfo.get("id"));

        User user = findOrCreateOAuth2User(email, name, AuthProvider.KAKAO, providerId);
        return issueTokens(user);
    }

    private User findOrCreateOAuth2User(String email, String name, AuthProvider provider, String providerId) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.updateOAuth2Info(name, providerId);
            return user;
        }

        User newUser = User.builder()
                .email(email)
                .name(name)
                .role(Role.USER)
                .provider(provider)
                .providerId(providerId)
                .build();
        return userRepository.save(newUser);
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        return new TokenResponse(accessToken, refreshToken);
    }
}
