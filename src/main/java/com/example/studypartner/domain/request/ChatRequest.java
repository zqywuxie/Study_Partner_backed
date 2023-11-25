package com.example.studypartner.domain.request;

import lombok.Data;

/**
 * @author wuxie
 * @date 2023/11/23 16:30
 * @description 清空聊天记录的请求类
 */

@Data
public class ChatRequest {
	/**
	 * 发送消息者id
	 */

	private Long formId;


	/**
	 * 接收消息者id
	 */
	private Long toId;

	/**
	 * 聊天群id
	 */
	private Long teamId;
}
