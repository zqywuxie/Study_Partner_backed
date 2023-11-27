package com.example.studypartner.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.studypartner.domain.entity.Follow;
import com.example.studypartner.domain.entity.Message;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.enums.MessageTypeEnum;
import com.example.studypartner.domain.vo.UserVO;
import com.example.studypartner.mapper.FollowMapper;
import com.example.studypartner.service.FollowService;
import com.example.studypartner.service.MessageService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.studypartner.constant.RedisConstants.*;

/**
 * @author wuxie
 * @description 针对表【follow】的数据库操作Service实现
 * @createDate 2023-11-20 10:02:50
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow>
		implements FollowService {

	@Resource
	@Lazy
	private UserService userService;

	@Resource
	private MessageService messageService;


	@Resource
	private RedisTemplate redisTemplate;

	@Override
	public void followUser(Long followUserId, Long userId) {
		LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
		followLambdaQueryWrapper.eq(Follow::getUserId, followUserId).eq(Follow::getFollowUserId, userId);
		long count = this.count(followLambdaQueryWrapper);
		if (count == 0) {
			Follow follow = new Follow();
			follow.setFollowUserId(userId);
			follow.setUserId(followUserId);
			this.save(follow);

			Message message = new Message();
			message.setType(MessageTypeEnum.FOLLOW_NOTIFICATIONS.getValue());
			message.setFromId(userId);
			message.setToId(followUserId);
			message.setData(String.valueOf(userId));
			messageService.save(message);
			String likeNumKey = MESSAGE_FOLLOW_MESSAGES_KEY + followUserId;
			Boolean hasKey = redisTemplate.hasKey(likeNumKey);
			if (Boolean.TRUE.equals(hasKey)) {
				redisTemplate.opsForValue().increment(likeNumKey);
			} else {
				redisTemplate.opsForValue().set(likeNumKey, "1");
			}
		} else {
			Message message = new Message();
			message.setType(MessageTypeEnum.FOLLOW_NOTIFICATIONS.getValue());
			message.setFromId(userId);
			message.setToId(followUserId);
			LambdaQueryWrapper<Message> messageLambdaQueryWrapper = new LambdaQueryWrapper<>();
			messageLambdaQueryWrapper.eq(Message::getType, MessageTypeEnum.FRIEND_APPLICATION.getValue())
					.eq(Message::getFromId, userId)
					.eq(Message::getToId, followUserId);
			messageService.remove(messageLambdaQueryWrapper);
			this.remove(followLambdaQueryWrapper);
		}
	}

	@Override
	public List<UserVO> listFans(Long userId) {
		LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
		followLambdaQueryWrapper.eq(Follow::getFollowUserId, userId);
		List<Follow> list = this.list(followLambdaQueryWrapper);
		if (list == null || list.size() == 0) {
			return new ArrayList<>();
		}
		List<User> userList = list.stream().map((follow -> userService.getById(follow.getUserId()))).filter(Objects::nonNull).collect(Collectors.toList());
		return userList.stream().map((item) -> {
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(item, userVO);
			LambdaQueryWrapper<Follow> lambdaQueryWrapper = new LambdaQueryWrapper<>();
			lambdaQueryWrapper.eq(Follow::getUserId, userId).eq(Follow::getFollowUserId, item.getId());
			long count = this.count(lambdaQueryWrapper);
			userVO.setIsFollow(count > 0);
			return userVO;
		}).collect(Collectors.toList());
	}

	@Override
	public List<UserVO> listMyFollow(Long userId) {
		LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
		followLambdaQueryWrapper.eq(Follow::getUserId, userId);
		List<Follow> list = this.list(followLambdaQueryWrapper);
		List<User> userList = list.stream().map((follow -> userService.getById(follow.getFollowUserId()))).collect(Collectors.toList());
		return userList.stream().map((user) -> {
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(user, userVO);
			userVO.setIsFollow(true);
			return userVO;
		}).collect(Collectors.toList());
	}

	@Override
	public Integer fansCount(Long loginUser) {
		String key = FANS_COUNT_KEY + loginUser;
		Integer cachedFansCount = (Integer) redisTemplate.opsForValue().get(key);

		if (cachedFansCount != null) {
			// If data is found in cache, return it directly
			return cachedFansCount;
		}
		LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
		followLambdaQueryWrapper.eq(Follow::getUserId, loginUser);
		long count = this.count(followLambdaQueryWrapper);
		redisTemplate.opsForValue().set(key, count);
		redisTemplate.expire(key, 1, TimeUnit.HOURS);
		return Math.toIntExact(count);
	}

	@Override
	public Integer myFollowCount(Long loginUser) {
		String key = FOLLOW_COUNT_KEY + loginUser;
		Integer cachedFansCount = (Integer) redisTemplate.opsForValue().get(key);

		if (cachedFansCount != null) {
			// If data is found in cache, return it directly
			return cachedFansCount;
		}
		LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
		followLambdaQueryWrapper.eq(Follow::getUserId, loginUser);
		long count = this.count(followLambdaQueryWrapper);
		redisTemplate.opsForValue().set(key, count);
		redisTemplate.expire(key, 1, TimeUnit.HOURS);
		return Math.toIntExact(count);
	}
}




