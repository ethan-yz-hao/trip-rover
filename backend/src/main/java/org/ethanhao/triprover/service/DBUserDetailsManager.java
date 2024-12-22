package org.ethanhao.triprover.service;

import java.util.List;
import java.util.Objects;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.Role;
import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.exception.UserOperationException;
import org.ethanhao.triprover.repository.MenuRepository;
import org.ethanhao.triprover.repository.RoleRepository;
import org.ethanhao.triprover.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

@Service
public class DBUserDetailsManager implements UserDetailsManager, UserDetailsPasswordService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // search User by username
        User user = userRepository.findByUserName(username);

        // if user not found, throw exception
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("Username Not Found");
        }

        // encapsulate user information in UserDetails implementation class
        List<String> list = menuRepository.findPermsByUserId(user.getId());

        return new LoginUser(user, list);
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        return null;
    }

    @Override
    public void createUser(UserDetails userDetails) {
    }

    public User createUserWithRole(User user, List<String> roles) {
        // no duplicate user with status 0
        if (userRepository.findByUserNameAndDelFlag(user.getUserName(), 0) != null) {
            throw new UserOperationException("User with the same username already exists");
        }
        // set user type and status
        user.setType(0);
        user.setStatus(0);
        // set delFlag
        user.setDelFlag(0);
        // encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // set user roles
        List<Role> roleList = roles.stream().map(roleRepository::findByRoleKey).toList();
        user.getRoles().addAll(roleList);
        return userRepository.save(user);
    }

    @Override
    public void updateUser(UserDetails user) {

    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        return false;
    }
}
