package dev.dolu.userservice.service;

import dev.dolu.userservice.models.AuthProvider;
import dev.dolu.userservice.models.Role;
import dev.dolu.userservice.models.User;
import dev.dolu.userservice.repository.UserRepository;
import dev.dolu.userservice.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtils jwtUtils;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = super.loadUser(request);
        Map<String, Object> attributes = oauthUser.getAttributes();

        String rawEmail = (String) attributes.get("email");
        String email = rawEmail != null ? rawEmail.toLowerCase() : null;

        String rawUsername = (String) attributes.get("login");
        String baseUsername = rawUsername != null ? rawUsername.toLowerCase() :
                (email != null ? email.split("@")[0].toLowerCase() : "user_" + UUID.randomUUID().toString().substring(0, 6));
        String uniqueUsername = baseUsername;
        int suffix = 1;
        while (userRepository.existsByUsername(uniqueUsername)) {
            uniqueUsername = baseUsername + suffix;
            suffix++;
        }

        String rawName = (String) attributes.get("name");
        String firstName = "oauth";
        String lastName = "user";

        if (rawName != null) {
            String[] split = rawName.trim().split(" ", 2);
            firstName = split.length > 0 ? split[0].toLowerCase() : "oauth";
            lastName = split.length > 1 ? split[1].toLowerCase() : "user";
        }

        String profileImage = (String) attributes.getOrDefault("picture", null);

        if (email != null) {
            User existing = userRepository.findByEmail(email);
            if (existing == null) {
                User newUser = new User();
                newUser.setUsername(uniqueUsername);
                newUser.setEmail(email);
                newUser.setFirstName(firstName);
                newUser.setLastName(lastName);
                newUser.setPhoneNumber("OAUTH_" + UUID.randomUUID().toString().substring(0, 8));
                newUser.setPassword("OAUTH2_USER");
                newUser.setEnabled(true);
                newUser.setVerified(true);
                newUser.setAuthProvider(AuthProvider.GOOGLE); // or GITHUB if it's GitHub
                newUser.setProfileImage(profileImage);
                newUser.setRole(Role.USER);
                userRepository.save(newUser);
            }
        }

        return oauthUser;
    }
}