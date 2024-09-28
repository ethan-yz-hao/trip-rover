package org.ethanhao.triprover.service.impl;

import jakarta.transaction.Transactional;
import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.Role;
import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.repository.MenuRepository;
import org.ethanhao.triprover.repository.RoleRepository;
import org.ethanhao.triprover.repository.UserRepository;
import org.ethanhao.triprover.service.CustomOAuth2UserService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class CustomOAuth2UserServiceImpl implements CustomOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Load user details from the OAuth2 provider
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        // Fetch or create the user in the database
        User user = loadOrCreateOAuthUser(oAuth2User);

        // Fetch permissions/roles for this user
        List<String> permissions = menuRepository.findPermsByUserId(user.getId());

        // Return a LoginUser object that integrates OAuth2 attributes and your custom user details
        return new LoginUser(user, permissions, oAuth2User.getAttributes());
    }

    @Transactional
    public User loadOrCreateOAuthUser(OAuth2User oAuth2User) {
        // Check if the user already exists in the database
        User user = userRepository.findByEmailAndDelFlag(oAuth2User.getAttribute("email"), 0);

        // if user not found, create a new user
        if (Objects.isNull(user)) {
            User newUser = new User();
            newUser.setEmail(oAuth2User.getAttribute("email"));
            newUser.setUserName(oAuth2User.getAttribute("name"));
            newUser.setNickName(oAuth2User.getAttribute("name"));
            newUser.setAvatar(oAuth2User.getAttribute("picture"));
            // set user type and status
            newUser.setType(1);
            newUser.setStatus(0);
            // set delFlag
            newUser.setDelFlag(0);
            // set user roles
            List<Role> roleList = Stream.of("user").map(roleRepository::findByRoleKey).toList();
            newUser.getRoles().addAll(roleList);

            // Save the new user to the database
            return userRepository.save(newUser);
        }

        return user;
    }
}
