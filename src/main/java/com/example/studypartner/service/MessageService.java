package com.example.studypartner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.studypartner.domain.entity.Message;
import com.example.studypartner.domain.vo.BlogVO;
import com.example.studypartner.domain.vo.MessageVO;

import java.util.List;


/**
 * @author wuxie
 * @description 针对表【message】的数据库操作Service
 * @createDate 2023-11-21 15:47:29
 */
public interface MessageService extends IService<Message> {


	/**
	 * 获得某种消息的数量 点赞 好友申请 评论
	 * @param userId
	 * @param type
	 * @return
	 */
	Long getMessageNum(Long userId,Integer type);

	/**
	 * 根据消息类型获得消息列表
	 *
	 * @param userId 用户ID
	 * @param type   消息类型
	 * @return 消息列表
	 */
	List<MessageVO> getMessages(Long userId, Integer type);

	List<MessageVO> getAllMessage(Long userId);


	Boolean readMessage(Long userId,Integer type);

}
