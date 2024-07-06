package com.example.studypartner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.studypartner.domain.entity.Chat;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.request.ChatClearRequest;
import com.example.studypartner.domain.request.ChatRequest;
import com.example.studypartner.domain.vo.ChatMessageVO;
import com.example.studypartner.domain.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;


/**
 * @author wuxie
 * @description 针对表【chat(聊天记录表)】的数据库操作Service
 * @createDate 2023-11-23 16:24:01
 */
public interface ChatService extends IService<Chat> {


	/**
	 * redis缓存相关方法
	 *
	 * @param redisKey
	 * @param id
	 * @return
	 */
	List<ChatMessageVO> getCache(String redisKey, String id);

	void saveCache(String redisKey, String id, List<ChatMessageVO> chatMessageVos);

	void deleteKey(String key, String id);

	/**
	 * 获得私聊，公开聊，群聊信息
	 *
	 * @param chatRequest
	 * @param chatType
	 * @param loginUser
	 * @return
	 */
	List<ChatMessageVO> getPrivateChat(ChatRequest chatRequest, int chatType, User loginUser);


	ChatMessageVO chatResult(Long userId, Long toId, String text, Integer chatType, Date createTime);


	List<ChatMessageVO> getTeamChat(ChatRequest chatRequest, int teamChat, User loginUser);

	List<ChatMessageVO> getHallChat(int chatType, User loginUser);

	/**
	 * 清空聊天记录
	 *
	 * @param chatClearRequest
	 * @return
	 */

	Boolean clearChatRecords(ChatClearRequest chatClearRequest);

	List<UserVO> getPrivateUser(User loginUser);
}
