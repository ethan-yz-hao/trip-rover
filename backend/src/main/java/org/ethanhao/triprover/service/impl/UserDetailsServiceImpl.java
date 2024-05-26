package org.ethanhao.triprover.service.impl;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.repository.MenuRepository;
import org.ethanhao.triprover.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // search User by username
        User user = userRepository.findByUserName(username);

        // if user not found, throw exception
        if (Objects.isNull(user)) {
            throw new RuntimeException("Username or password is incorrect");
        }

        // encapsulate user information in UserDetails implementation class
        List<String> list = menuRepository.findPermsByUserId(user.getId());

        return new LoginUser(user, list);
    }

}
