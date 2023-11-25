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

	@Test
	void Test() {
		User user = new User();
		user.setId(0L);
		user.setUsername("123");
		user.setUserAccount("123");
		user.setAvatarUrl("123");
		user.setGender(0);
		user.setUserPassword("123");
		user.setEmail("123");
		user.setUserStatus(0);
		user.setPhone("123");
		user.setCreateTime(new Date());
		user.setUpdateTime(new Date());
		user.setIsDelete(0);
		boolean save = userService.save(user);
		Assert.assertTrue(save);
	}

	// @Test
	// void Register() {
	//     String userAccount = "yupi";
	//     String userPassword = "";
	//     String checkPassword = "123456";
	//     long result = userService.Register(userAccount, userPassword, checkPassword);
	//     Assertions.assertEquals(-1, result);
	//
	//     userAccount = "yu";
	//     result = userService.Register(userAccount, userPassword, checkPassword);
	//     Assertions.assertEquals(-1, result);
	//
	//     userAccount = "yupi";
	//     userPassword = "123456";
	//     result = userService.Register(userAccount, userPassword, checkPassword);
	//     Assertions.assertEquals(-1, result);
	//
	//     userAccount = "yu pi";
	//     userPassword = "12345678";
	//     result = userService.Register(userAccount, userPassword, checkPassword);
	//     Assertions.assertEquals(-1, result);
	//
	//     checkPassword = "123456789";
	//     result = userService.Register(userAccount, userPassword, checkPassword);
	//     Assertions.assertEquals(-1, result);
	//
	//     userAccount = "dogyupi";
	//     checkPassword = "12345678";
	//     result = userService.Register(userAccount, userPassword, checkPassword);
	//     Assertions.assertEquals(-1, result);
	//
	//
	//
	//     result = userService.Register(userAccount, userPassword, checkPassword);
	//     Assertions.assertTrue(result > 0);
	// }
	@Test
	void Update() {
		User user = new User();
		user.setId(1L);
		user.setUsername("zqywuxie");
		user.setUserAccount("zqywuxie");
		user.setAvatarUrl("24");
		user.setGender(0);
		user.setUserPassword("24");
		user.setEmail("24");
		user.setUserStatus(0);
		user.setPhone("24");
		user.setCreateTime(new Date());
		user.setUpdateTime(new Date());
		user.setIsDelete(0);
		user.setUserRole(0);
		QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
		userQueryWrapper.eq("username", user.getUsername());
		userQueryWrapper.eq("userAccount", user.getUserAccount());
		User one = userService.getOne(userQueryWrapper);
		if (one == null) {
			System.out.println(false);
		} else {
			System.out.println(true);
		}
	}


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