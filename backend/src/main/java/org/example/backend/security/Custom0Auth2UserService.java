package org.example.backend.security;

import lombok.RequiredArgsConstructor;
import org.example.backend.model.AppUser;
import org.example.backend.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Custom0Auth2UserService extends DefaultOAuth2UserService {

    private final AppUserRepository appUserRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        OAuth2User oauthUser = super.loadUser(userRequest);
        AppUser appUser = appUserRepository.findById(oauthUser.getName())
                .orElseGet(() -> createAndSaveUser(oauthUser));

        return new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(appUser.getRole())), oauthUser.getAttributes(), "id");

    }

    private AppUser createAndSaveUser(OAuth2User oauthUser) {
        AppUser newUser = AppUser.builder()
                .id(oauthUser.getName())
                .username(oauthUser.getAttribute("login"))
                .email(oauthUser.getAttribute("email"))
                .role("USER")
                .build();

        return appUserRepository.save(newUser);
    }
}