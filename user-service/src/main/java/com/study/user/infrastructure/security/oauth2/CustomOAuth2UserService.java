package com.study.user.infrastructure.security.oauth2;

import com.study.user.domain.entity.AuthProvider;
import com.study.user.domain.entity.Role;
import com.study.user.domain.entity.User;
import com.study.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        String email;
        String name;
        String providerId;

        if (provider == AuthProvider.GOOGLE) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            providerId = oAuth2User.getAttribute("sub");
        } else if (provider == AuthProvider.KAKAO) {
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            email = (String) kakaoAccount.get("email");
            name = (String) profile.get("nickname");
            providerId = String.valueOf(oAuth2User.getAttribute("id"));
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.updateOAuth2Info(name, providerId);
        } else {
            user = User.builder()
                    .email(email)
                    .name(name)
                    .role(Role.USER)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
        }
        userRepository.save(user);

        return new DefaultOAuth2User(
                Collections.singleton(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
                Map.of("id", user.getId(), "email", email, "name", name),
                "email"
        );
    }
}
