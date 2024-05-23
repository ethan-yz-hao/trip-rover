package org.ethanhao.triprover;

import org.ethanhao.triprover.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelect() {
        userMapper.selectList(null).forEach(System.out::println);
    }
}
