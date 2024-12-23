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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private String defaultAvatarUrl;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // search User by username
        User user = userRepository.findByUserNameAndDelFlag(username, 0);

        // if user not found, throw exception
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("Username Not Found");
        }

        // encapsulate user information in UserDetails implementation class
        List<String> list = menuRepository.findPermsByUserId(user.getId());

        return new LoginUser(user, list);
    }

    @Override
    public void createUser(UserDetails userDetails) {
    }

    public User createUserWithRole(User user, List<String> roles) {
        // no duplicate user with status 0
        if (userExists(user.getUserName())) {
            throw new UserOperationException("User with the same username already exists");
        }
        // set default avatar
        user.setAvatar(defaultAvatarUrl);
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
    public void deleteUser(String username) {
        User user = userRepository.findByUserName(username);
        
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("Username Not Found");
        }

        user.setStatus(1); // set status to 1
        user.setDelFlag(1); // set delete flag to 1
        userRepository.save(user);
    }

    @Override
    public void updateUser(UserDetails user) {
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        return null;
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        if (currentUser == null) {
            throw new AccessDeniedException("Can't change password as no Authentication object found in context");
        }
        
        String username = currentUser.getName();
        User user = userRepository.findByUserNameAndDelFlag(username, 0);
        
        // Check if old password matches
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid old password");
        }
        
        // Update to new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.findByUserNameAndDelFlag(username, 0) != null;
    }
}
