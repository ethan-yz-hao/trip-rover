package org.ethanhao.triprover;

import org.ethanhao.triprover.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testBCrypt() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("password"));
        System.out.println(encoder.encode("password"));
        System.out.println(encoder.matches("password",
                "$2a$10$NEgAp4igmIoz6iOUTGB34ehY0aHNHblMZOobvmT44Pyq9rVuiwRlS"));
    }

    @Test
    public void testSelect() {
        userMapper.selectList(null).forEach(System.out::println);
    }
}
