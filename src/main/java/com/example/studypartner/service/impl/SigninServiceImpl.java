package com.example.studypartner.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.studypartner.domain.entity.Signin;
import com.example.studypartner.mapper.SigninMapper;
import com.example.studypartner.service.SigninService;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.example.studypartner.constant.UserConstant.USER_SIGN_KEY;

/**
 * @author wuxie
 * @description 针对表【signin】的数据库操作Service实现
 * @createDate 2024-01-13 18:58:43
 */
@Service
public class SigninServiceImpl extends ServiceImpl<SigninMapper, Signin>
		implements SigninService {


	@Resource
	private RedisTemplate redisTemplate;

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	/**
	 * 判断是否签到成功，加到userVo里面？ todo
	 * @param userId
	 * @return
	 */
	@Override
	public boolean signIn(Long userId) {
		// 2.获取日期 使用hutool的日期时间工具-DateUtil
		Date date = DateUtil.date();
		// 3.拼接key
		String keySuffix = DateUtil.format(date, ":yyyyMM");
		String key = USER_SIGN_KEY + userId + keySuffix;
		// 4.获取今天是本月的第几天
		int dayOfMonth = DateUtil.dayOfMonth(date);
		// 5.写入Redis SETBIT key offset 1
		Boolean isSignIn = stringRedisTemplate.opsForValue().getBit(key, dayOfMonth - 1);
		if (Boolean.TRUE.equals(isSignIn)) {
			return false;
		} else {
			stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
		}
		return true;
	}

	@Override
	public int signDays(Long userId) {
		// 2.获取日期 使用hutool的日期时间工具-DateUtil
		Date date = DateUtil.date();
		// 3.拼接key
		String keySuffix = DateUtil.format(date, ":yyyyMM");
		String key = USER_SIGN_KEY + userId + keySuffix;
		// 4.获取今天是本月的第几天
		int dayOfMonth = DateUtil.dayOfMonth(date);
		// 5.获取本月截止今天为止的所有的签到记录，返回的是一个十进制的数字 BITFIELD sign:999:202308 GET u18 0
		List<Long> result = stringRedisTemplate.opsForValue().bitField(
				key,
				BitFieldSubCommands.create()
						.get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
		);
		if (result == null || result.isEmpty()) {
			// 没有任何签到结果
			return 0;
		}
		//num为0，直接返回0
		Long num = result.get(0);
		if (num == null || num == 0) {
			return 0;
		}
		// 6.循环遍历
		int count = 0;
		// 如果为0，说明未签到，结束
		// 如果不为0，说明已签到，计数器+1
		// 与1做与运算，就能得到最后一位bit，因为前面全是0
		while ((num & 1) != 0) {
			// 6.1.让这个数字与1做与运算，得到数字的最后一个bit位  // 判断这个bit位是否为0
			count++;
			// 把数字右移一位，抛弃最后一个bit位，继续下一个bit位
			num >>>= 1;
		}
		return count;
	}
}








