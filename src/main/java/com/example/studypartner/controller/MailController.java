
package com.example.studypartner.controller;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.service.MailService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.example.studypartner.constant.EmailConstant.CAPTCHA_CACHE_KEY;


/**
 * @author wuxie
 * @CreateDate 2023/06/27 8:50
 * @description
 */
@RestController
@RequestMapping("/mail")
@Slf4j
public class MailController {

	@Autowired
	private MailService mailService;

	@Resource
	private UserService userService;

	@Resource
	private RedisTemplate<String, String> redisTemplate;

//    @GetMapping("/sendMail")
//    public String sendMail() {
//        try {
//            mailService.sendMail("573905257@qq.com",  "使用SpringBoot整合邮箱发送消息");
//            return "邮件发送成功";
//        } catch (MessagingException e) {
//            e.printStackTrace();
//            return "邮件发送失败";
//        }
//    }

	@GetMapping("/getCaptcha")
	public CommonResult<String> getCaptcha(@RequestParam String email) {
		if (StringUtils.isBlank(email)) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
		if (!Pattern.matches(emailPattern, email)) {
			return ResultUtils.failed(ErrorCode.PARAMS_ERROR, "不合法的邮箱地址！");
		}

		Boolean hasKey = redisTemplate.hasKey(CAPTCHA_CACHE_KEY + email);
		if (Boolean.TRUE.equals(hasKey)) {
			String captcha = redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + email);
			return ResultUtils.success(captcha);
		}

//		// 验证是否存在
//		LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
//		wrapper.eq(User::getEmail, email);
//		User user = userService.getOne(wrapper);
//		if (user == null) {
//			return ResultUtils.failed(ErrorCode.NULL_ERROR, "该邮箱未注册用户");
//		}
		String captcha = RandomUtil.randomNumbers(4);
		try {
			mailService.sendMail(email, captcha);
			redisTemplate.opsForValue().set(CAPTCHA_CACHE_KEY + email, captcha, 2, TimeUnit.MINUTES);
			return ResultUtils.success(captcha);
		} catch (Exception e) {
			log.error("【发送验证码失败】" + e.getMessage());
			return ResultUtils.failed(ErrorCode.OPERATION_ERROR, "验证码获取失败");
		}
	}


	@GetMapping("/getCaptcha/{email}")
	public CommonResult<String> getCaptchaFromRedis(@PathVariable String email) {
		String key = CAPTCHA_CACHE_KEY + email;
		Boolean hasKey = redisTemplate.hasKey(key);
		if (Boolean.TRUE.equals(hasKey)) {
			String s = redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + email);
			return ResultUtils.success(s);
		} else {
			return ResultUtils.failed(ErrorCode.NULL_ERROR);
		}
	}

}
