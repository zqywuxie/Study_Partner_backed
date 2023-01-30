package com.example.studypartner;

import com.example.studypartner.mapper.UserMapper;
import com.example.studypartner.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SampleTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;
    @Test
    public void testSelect() {
        userService.Register("456789","123456","123456");
    }

}
