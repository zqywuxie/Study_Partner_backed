package com.example.studypartner.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.Chat;
import com.example.studypartner.domain.entity.Team;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.request.ChatClearRequest;
import com.example.studypartner.domain.request.ChatRequest;
import com.example.studypartner.domain.vo.ChatMessageVO;
import com.example.studypartner.domain.vo.UserVO;
import com.example.studypartner.domain.vo.WebSocketVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.mapper.ChatMapper;
import com.example.studypartner.service.ChatService;
import com.example.studypartner.service.TeamService;
import com.example.studypartner.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.studypartner.constant.ChatConstant.*;
import static com.example.studypartner.constant.UserConstant.ADMIN_ROLE;

/**
 * @author wuxie
 * @description 针对表【chat(聊天记录表)】的数据库操作Service实现
 * @createDate 2023-11-23 16:24:01
 */
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat>
		implements ChatService {


	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	@Resource
	private UserService userService;

	@Resource
	private TeamService teamService;

	@Override
	public List<ChatMessageVO> getPrivateChat(ChatRequest chatRequest, int chatType, User loginUser) {
		Long toId = chatRequest.getToId();
		if (toId == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}

		List<ChatMessageVO> chatRecords = getCache(CACHE_CHAT_PRIVATE, loginUser.getId() + String.valueOf(toId));
		if (chatRecords != null) {
			saveCache(CACHE_CHAT_PRIVATE, loginUser.getId() + String.valueOf(toId), chatRecords);
			return chatRecords;
		}


		LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
		chatLambdaQueryWrapper.
				and(privateChat -> privateChat.eq(Chat::getFromId, loginUser.getId()).eq(Chat::getToId, toId)
						.or().
						eq(Chat::getToId, loginUser.getId()).eq(Chat::getFromId, toId)
				).eq(Chat::getChatType, chatType);
		// 两方共有聊天
		List<Chat> list = this.list(chatLambdaQueryWrapper);

		List<ChatMessageVO> chatMessageVOS = list.stream().map(chat -> {
			ChatMessageVO chatMessageVO = chatResult(loginUser.getId(), toId, chat.getContent(), chatType, chat.getCreateTime());
			if (chat.getFromId().equals(loginUser.getId())) {
				chatMessageVO.setIsMy(true);
			}
			return chatMessageVO;
		}).collect(Collectors.toList());
		saveCache(CACHE_CHAT_PRIVATE, loginUser.getId() + String.valueOf(toId), chatMessageVOS);
		return chatMessageVOS;
	}

	@Override
	public List<ChatMessageVO> getCache(String redisKey, String id) {
		ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
		List<ChatMessageVO> chatRecords;
		if (redisKey.equals(CACHE_CHAT_HALL)) {
			chatRecords = (List<ChatMessageVO>) valueOperations.get(redisKey);
		} else {
			chatRecords = (List<ChatMessageVO>) valueOperations.get(redisKey + id);
		}
		return chatRecords;
	}

	@Override
	public void saveCache(String redisKey, String id, List<ChatMessageVO> chatMessageVOS) {
		try {
			ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
			// 解决缓存雪崩
			int i = RandomUtil.randomInt(2, 3);
			if (redisKey.equals(CACHE_CHAT_HALL)) {
				valueOperations.set(redisKey, chatMessageVOS, 2 + i / 10, TimeUnit.MINUTES);
			} else {
				valueOperations.set(redisKey + id, chatMessageVOS, 2 + i / 10, TimeUnit.MINUTES);
			}
		} catch (Exception e) {
			log.error("redis set key error");
		}
	}

	private ChatMessageVO chatResult(Long userId, String text) {
		ChatMessageVO chatMessageVO = new ChatMessageVO();
		User fromUser = userService.getById(userId);
		WebSocketVO fromWebSocketVo = new WebSocketVO();
		BeanUtils.copyProperties(fromUser, fromWebSocketVo);
		chatMessageVO.setFromUser(fromWebSocketVo);
		chatMessageVO.setText(text);
		return chatMessageVO;
	}

	@Override
	public ChatMessageVO chatResult(Long userId, Long toId, String text, Integer chatType, Date createTime) {
		ChatMessageVO chatMessageVO = new ChatMessageVO();
		User fromUser = userService.getById(userId);
		User toUser = userService.getById(toId);
		WebSocketVO fromWebSocketVo = new WebSocketVO();
		WebSocketVO toWebSocketVo = new WebSocketVO();
		BeanUtils.copyProperties(fromUser, fromWebSocketVo);
		BeanUtils.copyProperties(toUser, toWebSocketVo);
		chatMessageVO.setFromUser(fromWebSocketVo);
		chatMessageVO.setToUser(toWebSocketVo);
		chatMessageVO.setChatType(chatType);
		chatMessageVO.setText(text);
		chatMessageVO.setCreateTime(DateUtil.format(createTime, "yyyy-MM-dd HH:mm:ss"));
		return chatMessageVO;
	}

	@Override
	public void deleteKey(String key, String id) {
		if (key.equals(CACHE_CHAT_HALL)) {
			redisTemplate.delete(key);
		} else {
			redisTemplate.delete(key + id);
		}
	}

	@Override
	public List<ChatMessageVO> getTeamChat(ChatRequest chatRequest, int chatType, User loginUser) {
		Long teamId = chatRequest.getTeamId();
		if (teamId == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "请求有误");
		}
		List<ChatMessageVO> chatRecords = getCache(CACHE_CHAT_TEAM, String.valueOf(teamId));
		if (!chatRecords.isEmpty()) {
			List<ChatMessageVO> chatMessageVOS = checkIsMyMessage(loginUser, chatRecords);
			saveCache(CACHE_CHAT_TEAM, String.valueOf(teamId), chatMessageVOS);
			return chatMessageVOS;
		}
		Team team = teamService.getById(teamId);
		LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
		chatLambdaQueryWrapper.eq(Chat::getChatType, chatType).eq(Chat::getTeamId, teamId);
		List<ChatMessageVO> chatMessageVOS = returnMessage(loginUser, team.getUserId(), chatLambdaQueryWrapper);
		saveCache(CACHE_CHAT_TEAM, String.valueOf(teamId), chatMessageVOS);
		return chatMessageVOS;
	}

	@Override
	public List<ChatMessageVO> getHallChat(int chatType, User loginUser) {
		List<ChatMessageVO> chatRecords = getCache(CACHE_CHAT_HALL, String.valueOf(loginUser.getId()));
		if (chatRecords != null) {
			List<ChatMessageVO> chatMessageVOS = checkIsMyMessage(loginUser, chatRecords);
			saveCache(CACHE_CHAT_HALL, String.valueOf(loginUser.getId()), chatMessageVOS);
			return chatMessageVOS;
		}
		LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
		chatLambdaQueryWrapper.eq(Chat::getChatType, chatType);
		List<ChatMessageVO> chatMessageVOS = returnMessage(loginUser, null, chatLambdaQueryWrapper);
		saveCache(CACHE_CHAT_HALL, String.valueOf(loginUser.getId()), chatMessageVOS);
		return chatMessageVOS;
	}

	//todo 清空聊天记录
	@Override
	public Boolean clearChatRecords(ChatClearRequest chatClearRequest) {
		return null;
	}

	//todo
	@Override
	public List<UserVO> getPrivateUser(User loginUser) {
		return userService.list(new LambdaQueryWrapper<User>()
				.in(User::getId, this.list(
						new LambdaQueryWrapper<Chat>()
								.select(Chat::getToId)
								.eq(Chat::getFromId, loginUser.getId())
								.groupBy(Chat::getToId)
								.isNotNull(Chat::getToId)
				).stream().map(Chat::getToId).collect(Collectors.toList()))
		).stream().map(user -> {
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(user, userVO);
			return userVO;
		}).collect(Collectors.toList());
	}

	private List<ChatMessageVO> checkIsMyMessage(User loginUser, List<ChatMessageVO> chatRecords) {
		return chatRecords.stream().peek(chat -> {
			if (!chat.getFromUser().getId().equals(loginUser.getId()) && chat.getIsMy()) {
				chat.setIsMy(false);
			}
			if (Objects.equals(chat.getFromUser().getId(), loginUser.getId()) && !chat.getIsMy()) {
				chat.setIsMy(true);
			}
		}).collect(Collectors.toList());
	}

	private List<ChatMessageVO> returnMessage(User loginUser, Long
			userId, LambdaQueryWrapper<Chat> chatLambdaQueryWrapper) {
		List<Chat> chatList = this.list(chatLambdaQueryWrapper);
		return chatList.stream().map(chat -> {
			ChatMessageVO chatMessageVO = chatResult(chat.getFromId(), chat.getContent());
			boolean isCaptain = userId != null && userId.equals(chat.getFromId());
			if (isCaptain) {
				chatMessageVO.setIsAdmin(true);
			}
			if (chat.getFromId().equals(loginUser.getId())) {
				chatMessageVO.setIsMy(true);
			}
			chatMessageVO.setCreateTime(DateUtil.format(chat.getCreateTime(), "yyyy年MM月dd日 HH:mm:ss"));
			return chatMessageVO;
		}).collect(Collectors.toList());
	}
}




