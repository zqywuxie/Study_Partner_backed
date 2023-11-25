package com.example.studypartner.domain.request;

/**
 * @author wuxie
 * @date 2023/11/23 16:30
 * @description 清空聊天记录的请求类
 */
public class ChatClearRequest {

	/**
	 * 发送消息id
	 */
	private Long formId;

	/**
	 * 接收消息id
	 */
	private Long toId;
}
