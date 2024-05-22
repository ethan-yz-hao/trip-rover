package org.ethanhao.triprover.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserManagerConfig implements UserDetailsService {

    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String emailId) throws UsernameNotFoundException {
        return userRepo
                .findByEmailId(emailId)
                .map(UserConfig::new)
                .orElseThrow(() -> new UsernameNotFoundException("UserEmail: " + emailId + " does not exist"));
    }
}

