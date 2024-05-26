package org.ethanhao.triprover;

import org.ethanhao.triprover.repository.MenuRepository;
import org.ethanhao.triprover.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@SpringBootTest
public class RepositoryTest {

    @Autowired
    private UserRepository userRepository;

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
        userRepository.findAll().forEach(user -> {
            System.out.println("User ID: " + user.getId() + ", Username: " + user.getUserName());
        });
    }

    @Autowired
    private MenuRepository menuRepository;

    @Test
    public void testSelectPermsByUserId() {
        List<String> list = menuRepository.findPermsByUserId(1L);
        System.out.println(list);
    }
}
