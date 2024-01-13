package com.example.studypartner.constant;

/**
 * 2022/10/11
 * 用户内容相关的常量
 *
 * @version 1.0
 * @Author:zqy
 */
public interface UserConstant {

	String USER_LOGIN_STATUS = "userLoginStatus";
	/**
	 * 管理员权限
	 */
	Integer ADMIN_ROLE = 1;
	/**
	 * 普通用户权限
	 */
	Integer DEFAULT_ROLE = 0;

	/**
	 * 用户默认头像
	 */
	String DEFAULT_AVATAR = "https://wuxie-image.oss-cn-chengdu.aliyuncs.com/2023/09/19/QQ图片20230807232937.jpg";

	String DEFAULT_NAME = "Default";


	/*
	签到key
	 */
	String USER_SIGN_KEY = "wuxie:user:sigin:";


}
