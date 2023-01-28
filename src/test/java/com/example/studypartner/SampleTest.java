package com.example.studypartner;

import com.example.studypartner.domain.User;
import com.example.studypartner.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SampleTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelect() {
        User user = userMapper.selectById(1);
        System.out.println(user);
    }

}
