package com.example.studypartner.constant;

/**
 * Redis常量
 *
 * @author wuxie
 * @date 2023/06/22
 */
public interface RedisConstants {
	String LOGIN_USER_KEY = "wuxie:login:token:";

	Long LOGIN_USER_TTL = 300L;
	/**
	 * 注册验证码键
	 */
	String REGISTER_CODE_KEY = "wuxie:register:";
	/**
	 * 注册验证码过期时间
	 */
	Long REGISTER_CODE_TTL = 15L;
	/**
	 * 用户更新电话键
	 */
	String USER_UPDATE_PHONE_KEY = "suer:user:update:phone:";
	/**
	 * 用户更新电话过期时间
	 */
	Long USER_UPDATE_PHONE_TTL = 30L;
	/**
	 * 用户更新邮件键
	 */
	String USER_UPDATE_EMAIL_KEY = "suer:user:update:email:";
	/**
	 * 用户更新邮件过期时间
	 */
	Long USER_UPDATE_EMAIl_TTL = 15L;
	/**
	 * 用户忘记密码键
	 */
	String USER_FORGET_PASSWORD_KEY = "wuxie:user:forget:";
	/**
	 * 用户忘记密码过期时间
	 */
	Long USER_FORGET_PASSWORD_TTL = 15L;
	/**
	 * 博客推送键
	 */
	String BLOG_FEED_KEY = "wuxie:feed:blog:";
	/**
	 * 新博文消息键
	 */
	String MESSAGE_BLOG_NUM_KEY = "wuxie:message:blog:num:";
	/**
	 * 新点赞消息键
	 */
	String MESSAGE_LIKE_NUM_KEY = "wuxie:message:like:num:";
	/**
	 * 新评论点赞消息键
	 */
	String MESSAGE_COMMENT_NUM_KEY = "wuxie:message:comment:num:";

	/**
	 * 新好友申请消息键
	 */
	String MESSAGE_FRIEND_APPLICATION_KEY = "wuxie:message:application:num:";
	/**
	 * 新系统消息键
	 */
	String MESSAGE_SYSTEM_MESSAGES_KEY = "wuxie:message:system:num:";
	/**
	 * 新系统消息键
	 */
	String MESSAGE_FOLLOW_MESSAGES_KEY = "wuxie:message:follow:num:";
	/**
	 * 用户推荐缓存
	 */
	String USER_RECOMMEND_KEY = "wuxie:user:recommend:";


	// 一天限改两次头像

	String AVATAR_UPDATE_KEY = "wuxie:user:update:avatar:";


	String LIKE_COUNT_KEY = "wuxie:user:like:count:";
	String FANS_COUNT_KEY = "wuxie:user:fans:count:";
	String FOLLOW_COUNT_KEY = "wuxie:user:follow:count:";
	Long AVATAR_UPDATE_TTL = 24L;



}
