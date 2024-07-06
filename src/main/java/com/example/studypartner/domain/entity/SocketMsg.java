package com.example.studypartner.domain.entity;

import lombok.Data;

/**
 * @authoer:wuxie
 * @Date: 2022/11/7
 * @description:
 */
@Data
public class SocketMsg {
	/**
	 * 聊天类型0：群聊，1：单聊
	 **/
	private int type;
	/**
	 * 发送者
	 **/
	private String fromUser;
	/**
	 * 接受者
	 **/
	private String toUser;
	/**
	 * 消息
	 **/
	private String msg;
}