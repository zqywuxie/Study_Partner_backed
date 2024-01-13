package com.example.studypartner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.FriendApplication;
import com.example.studypartner.domain.entity.Message;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.enums.MessageTypeEnum;
import com.example.studypartner.domain.vo.*;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.mapper.MessageMapper;
import com.example.studypartner.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.studypartner.constant.RedisConstants.*;


/**
 * @author Shier
 * @description 针对表【message】的数据库操作Service实现
 * @createDate 2023-11-21 17:39:30
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
		implements MessageService {

	@Resource
	@Lazy
	private CommentsService blogCommentsService;

	@Resource
	@Lazy
	private BlogService blogService;

	@Resource
	@Lazy
	private UserService userService;

	@Resource
	private FriendApplicationService friendApplicationService;

	@Resource
	private FollowService followService;

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Override
	public Long getMessageNum(Long userId, Integer type) {
		LambdaQueryWrapper<Message> messageLambdaQueryWrapper = new LambdaQueryWrapper<>();
		messageLambdaQueryWrapper.eq(Message::getToId, userId).eq(Message::getIsRead, 0).eq(Message::getType, type);
		return this.count(messageLambdaQueryWrapper);
	}


	@Override
	public List<MessageVO> getMessages(Long userId, Integer type) {
		LambdaQueryWrapper<Message> messageLambdaQueryWrapper = new LambdaQueryWrapper<>();
		messageLambdaQueryWrapper.eq(Message::getToId, userId)
				.and(wp -> wp.eq(Message::getType, type))
				.orderBy(true, false, Message::getCreateTime);
		List<Message> messageList = this.list(messageLambdaQueryWrapper);
		// 空
		if (messageList.isEmpty()) {
			return new ArrayList<>();
		}

		// 已读
//		LambdaUpdateWrapper<Message> messageLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
//		messageLambdaUpdateWrapper.eq(Message::getToId, userId).eq(Message::getType, type).set(Message::getIsRead, 1);
//		this.update(messageLambdaUpdateWrapper);
//		String likeNumKey = MESSAGE_LIKE_NUM_KEY + userId;
//		Boolean hasLike = stringRedisTemplate.hasKey(likeNumKey);
//		if (Boolean.TRUE.equals(hasLike)) {
//			stringRedisTemplate.opsForValue().set(likeNumKey, "0");
//		}

		return messageList.stream().map((item) -> {
			MessageVO messageVO = new MessageVO();
			User user = userService.getById(item.getFromId());
			if (user == null) {
				throw new ResultException(ErrorCode.PARAMS_ERROR, "发送人不存在");
			}
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(user, userVO);
			messageVO.setFromUser(userVO);
			// 评论 点赞 关注 好友申请

			//fromUser 评论内容 博文封面

			//fromUser 点赞内容（博文标题/评论）

			//fromUser

			//fromUser 申请内容
			if (item.getType() == MessageTypeEnum.BLOG_COMMENT_LIKE.getValue()) {
				CommentsVO commentsVO = blogCommentsService.getComment(Long.parseLong(item.getData()), userId);
				BlogVO blogVO = blogService.getBlogById(commentsVO.getBlogId());
				messageVO.setComment(commentsVO);
				messageVO.setBlog(blogVO);
			}
			if (item.getType() == MessageTypeEnum.BLOG_LIKE.getValue()) {
				BlogVO blogVO = blogService.getBlogById(Long.parseLong(item.getData()), userId);
				messageVO.setBlog(blogVO);
			}
			if (item.getType() == MessageTypeEnum.COMMENT_ADD.getValue()) {
				CommentsVO commentsVO = blogCommentsService.getComment(Long.parseLong(item.getData()), item.getFromId());
				BlogVO blogVO = blogService.getBlogById(commentsVO.getBlogId(), userId);
				messageVO.setBlog(blogVO);
				messageVO.setComment(commentsVO);
			}
			if (item.getType() == MessageTypeEnum.FRIEND_APPLICATION.getValue()) {
				LambdaQueryWrapper<FriendApplication> queryWrapper = new LambdaQueryWrapper<>();
				queryWrapper.eq(FriendApplication::getFromId, item.getData()).eq(FriendApplication::getReceiveId, userId);
				FriendApplication friendApplication = friendApplicationService.getOne(queryWrapper);
				FriendsRecordVO friendsRecordVO = new FriendsRecordVO();
				friendsRecordVO.setApplyUser(userVO);
				friendsRecordVO.setRemark(friendApplication.getRemark());
				friendsRecordVO.setStatus(friendApplication.getStatus());
				messageVO.setFriendsRecordVO(friendsRecordVO);
			}
			messageVO.setType(item.getType());
			messageVO.setCreateTime(item.getCreateTime());
			return messageVO;
		}).collect(Collectors.toList());
	}

	@Override
	public List<MessageVO> getAllMessage(Long userId) {
		List<MessageVO> messageVOList = new ArrayList<>();
		for (int i = 0; i <= 5; i++) {
			messageVOList.addAll(getMessages(userId, i));
		}
		return messageVOList;
	}

	@Override
	public Boolean readMessage(Long userId, Integer type) {
		LambdaUpdateWrapper<Message> messageLambdaQueryWrapper = new LambdaUpdateWrapper<>();
		messageLambdaQueryWrapper.eq(Message::getToId, userId).eq(Message::getType, type).set(Message::getIsRead, 1);
		return this.update(messageLambdaQueryWrapper);
	}

//	@Override
//	public List<BlogVO> getUserBlog(Long userId) {
//		String key = BLOG_FEED_KEY + userId;
//		Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
//				.reverseRangeByScoreWithScores(key, 0, System.currentTimeMillis(), 0, 10);
//		if (typedTuples == null || typedTuples.size() == 0) {
//			return new ArrayList<>();
//		}
//		ArrayList<BlogVO> blogVOList = new ArrayList<>(typedTuples.size());
//		for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
//			long blogId = Long.parseLong(Objects.requireNonNull(tuple.getValue()));
//			BlogVO blogVO = blogService.getBlogById(blogId, userId);
//			blogVOList.add(blogVO);
//		}
//		String likeNumKey = MESSAGE_BLOG_NUM_KEY + userId;
//		Boolean hasKey = stringRedisTemplate.hasKey(likeNumKey);
//		if (Boolean.TRUE.equals(hasKey)) {
//			stringRedisTemplate.opsForValue().set(likeNumKey, "0");
//		}
//		return blogVOList;
//	}

}




