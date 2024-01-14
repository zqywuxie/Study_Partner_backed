package com.example.studypartner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.studypartner.domain.entity.Signin;

/**
* @author wuxie
* @description 针对表【signin】的数据库操作Service
* @createDate 2024-01-13 18:58:43
*/
public interface SigninService extends IService<Signin> {


	/**
	 * 连续签到天数
	 * @param userId
	 * @return
	 */
	boolean signIn(Long userId);

	/**
	 * 连续签到天数
	 * @param userId
	 * @return
	 */
	int signDays(Long userId);

}
