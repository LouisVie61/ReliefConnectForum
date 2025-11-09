package demo.reliefconnectforum.service.auth.impl;

import demo.reliefconnectforum.Enum.UserRoleEnum;
import demo.reliefconnectforum.entity.User;
import demo.reliefconnectforum.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) {
        OAuth2User oAuth2User = super.loadUser(req);

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new IllegalStateException("Email not provided by provider");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail(email);
            user.setUsername(email);
            user.setFullName(oAuth2User.getAttribute("name"));
            user.setRole(UserRoleEnum.USER);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(user.getRole().name())),
                oAuth2User.getAttributes(),
                "email"
        );
    }
}