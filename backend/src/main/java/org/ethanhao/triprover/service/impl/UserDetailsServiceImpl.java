package org.ethanhao.triprover.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // search User by username
        LambdaQueryWrapper wrapper = new LambdaQueryWrapper<User>().eq(User::getUserName, username);
        User user = userMapper.selectOne(wrapper);

        // if user not found, throw exception
        if (Objects.isNull(user)) {
            throw new RuntimeException("Username or password is incorrect");
        }

        // encapsulate user information in UserDetails implementation class
        List<String> list = new ArrayList<>(Arrays.asList("test", "admin"));

        return new LoginUser(user, list);
    }

}
