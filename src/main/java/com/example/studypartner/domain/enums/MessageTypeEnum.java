package com.example.studypartner.domain.enums;

/**
 * 消息类型枚举
 *
 * @author wuxie
 * @date 2023/06/22
 */
public enum MessageTypeEnum {
	/**
	 * 博客就像
	 */
	BLOG_LIKE(0, "博文点赞"),
	/**
	 * 博客评论等
	 */
	BLOG_COMMENT_LIKE(1, "博文评论点赞"),
	COMMENT_ADD(2, "博文评论点赞"),
	FRIEND_APPLICATION(3, "好友申请"),
	SYSTEM_MESSAGES(4, "系统消息"),
	FOLLOW_NOTIFICATIONS(5, "关注通知");

	/**
	 * 价值
	 */
	private int value;
	/**
	 * 文本
	 */
	private String text;

	/**
	 * 消息类型枚举
	 *
	 * @param value 价值
	 * @param text  文本
	 */
	MessageTypeEnum(int value, String text) {
		this.value = value;
		this.text = text;
	}

	/**
	 * 获得价值
	 *
	 * @return int
	 */
	public int getValue() {
		return value;
	}

	/**
	 * 设置值
	 *
	 * @param value 价值
	 * @return {@link MessageTypeEnum}
	 */
	public MessageTypeEnum setValue(int value) {
		this.value = value;
		return this;
	}

	/**
	 * 得到文本
	 *
	 * @return {@link String}
	 */
	public String getText() {
		return text;
	}

	/**
	 * 设置文本
	 *
	 * @param text 文本
	 * @return {@link MessageTypeEnum}
	 */
	public MessageTypeEnum setText(String text) {
		this.text = text;
		return this;
	}
}
