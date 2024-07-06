package com.example.studypartner.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studypartner.domain.entity.User;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 2022/10/10
 * 用户信息测试
 *
 * @version 1.0
 * @Author:zqy
 */
@SpringBootTest
class UserServiceTest {
	@Resource
	private UserService userService;



	// @Test
	// void Register() {
	//     String useraccount = "yupi";
	//     String password = "";
	//     String checkPassword = "123456";
	//     long result = userService.Register(useraccount, password, checkPassword);
	//     Assertions.assertEquals(-1, result);
	//
	//     useraccount = "yu";
	//     result = userService.Register(useraccount, password, checkPassword);
	//     Assertions.assertEquals(-1, result);
	//
	//     useraccount = "yupi";
	//     password = "123456";
	//     result = userService.Register(useraccount, password, checkPassword);
	//     Assertions.assertEquals(-1, result);
	//
	//     useraccount = "yu pi";
	//     password = "12345678";
	//     result = userService.Register(useraccount, password, checkPassword);
	//     Assertions.assertEquals(-1, result);
	//
	//     checkPassword = "123456789";
	//     result = userService.Register(useraccount, password, checkPassword);
	//     Assertions.assertEquals(-1, result);
	//
	//     useraccount = "dogyupi";
	//     checkPassword = "12345678";
	//     result = userService.Register(useraccount, password, checkPassword);
	//     Assertions.assertEquals(-1, result);
	//
	//
	//
	//     result = userService.Register(useraccount, password, checkPassword);
	//     Assertions.assertTrue(result > 0);
	// }



	@Test
	void serachUserByTags() {
		List<String> java = Arrays.asList("java", "python");
		List<User> commonResults = userService.memorySearch(java);
		System.out.println(commonResults);
		Assert.assertNotNull(commonResults);
	}

	@Test
	void say() {

	}
}