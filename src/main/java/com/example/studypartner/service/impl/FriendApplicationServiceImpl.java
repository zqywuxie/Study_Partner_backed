package com.example.studypartner.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.FriendApplication;
import com.example.studypartner.domain.entity.Message;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.enums.MessageTypeEnum;
import com.example.studypartner.domain.request.FriendAddRequest;
import com.example.studypartner.domain.vo.FriendsRecordVO;
import com.example.studypartner.domain.vo.UserVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.mapper.FriendApplicationMapper;
import com.example.studypartner.service.FriendApplicationService;
import com.example.studypartner.service.MessageService;
import com.example.studypartner.service.UserService;
import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;
import com.mchange.lang.LongUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.example.studypartner.constant.FriendConstant.*;
import static com.example.studypartner.constant.RedisConstants.MESSAGE_COMMENT_NUM_KEY;
import static com.example.studypartner.constant.RedissonContents.APPLY_LOCK;


/**
 * @author wuxie
 * @description 针对表【FriendApplication(好友申请管理表)】的数据库操作Service实现
 * @createDate 2023-11-18 14:10:45
 */
@Service
public class FriendApplicationServiceImpl extends ServiceImpl<FriendApplicationMapper, FriendApplication>
		implements FriendApplicationService {

	@Resource
	private UserService userService;

	@Resource
	private RedissonClient redissonClient;

	@Resource
	private MessageService messageService;

	@Resource
	private RedisTemplate redisTemplate;


	Gson gson = new Gson();

	/**
	 * 添加好友
	 *
	 * @param loginUser
	 * @param friendAddRequest
	 * @return
	 */
	@Override
	@Transactional
	public boolean addFriendRecords(User loginUser, FriendAddRequest friendAddRequest) {
		Long receiveId = friendAddRequest.getReceiveId();
		String remark = friendAddRequest.getRemark();
		Long loginUserId = loginUser.getId();


		if (StringUtils.isNotBlank(remark) && remark.length() > 120) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "验证信息最多120个字符");
		}
		if (ObjectUtils.anyNull(loginUserId, friendAddRequest.getReceiveId())) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "添加失败");
		}
		// 1.添加的不能是自己
		if (Objects.equals(loginUserId, receiveId)) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "不能添加自己为好友");
		}

		// 防止同一时间多次添加
		RLock lock = redissonClient.getLock(APPLY_LOCK + loginUserId);
		try {
			// 抢到锁并执行
			if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
				// 2.条数大于等于1 就不能再添加
				LambdaQueryWrapper<FriendApplication> friendApplicationLambdaQueryWrapper = new LambdaQueryWrapper<>();
				friendApplicationLambdaQueryWrapper.eq(FriendApplication::getReceiveId, receiveId);
				friendApplicationLambdaQueryWrapper.eq(FriendApplication::getFromId, loginUserId);
				List<FriendApplication> list = this.list(friendApplicationLambdaQueryWrapper);
				list.forEach(friendApplication -> {
					if (list.size() > 1 && friendApplication.getStatus() == DEFAULT_STATUS) {
						throw new ResultException(ErrorCode.PARAMS_ERROR, "不能重复申请");
					}
				});
				FriendApplication newFriend = new FriendApplication();
				newFriend.setFromId(loginUserId);
				newFriend.setReceiveId(receiveId);
				if (StringUtils.isBlank(remark)) {
					newFriend.setRemark("我是" + userService.getById(loginUserId).getUsername() + ",请求添加好友");
				} else {
					newFriend.setRemark(remark);
				}


				String friendApplication = MESSAGE_COMMENT_NUM_KEY + receiveId;
				Boolean hasKey = redisTemplate.hasKey(friendApplication);
				if (Boolean.TRUE.equals(hasKey)) {
					redisTemplate.opsForValue().increment(friendApplication);
				} else {
					redisTemplate.opsForValue().set(friendApplication, 1);
				}


				return this.save(newFriend);
			}
		} catch (InterruptedException e) {
			log.error("add friend ：", e);
			return false;
		} finally {
			// todo 添加消息
			LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper.eq(Message::getType, MessageTypeEnum.FRIEND_APPLICATION.getValue())
					.eq(Message::getFromId, loginUserId)
					.eq(Message::getToId, receiveId)
					.eq(Message::getData, String.valueOf(loginUser.getId()));
			long count = messageService.count(queryWrapper);
			if (count == 0) {
				Message message = new Message();
				message.setType(MessageTypeEnum.FRIEND_APPLICATION.getValue());
				message.setFromId(loginUserId);
				message.setToId(receiveId);
				message.setData(String.valueOf(loginUser.getId()));

				messageService.save(message);
			}

			// 只能释放自己的锁
			if (lock.isHeldByCurrentThread()) {
				System.out.println("unLock: " + Thread.currentThread().getId());
				lock.unlock();
			}
		}
		return false;
	}

	@Override
	@Transactional
	public boolean deleteFriendRecords(User loginUser, Long friendId) {
		// todo loginUser更新数据后还是原来session里面的
		User user = userService.getById(loginUser.getId());
		String friendsIds = user.getFriendsIds();
		List<Long> friendsList = parseFriendsIds(friendsIds);

		if (!friendsList.contains(friendId)) {
			throw new ResultException(ErrorCode.NULL_ERROR, "该好友不存在");
		}

		// Update the current user's friends list
		updateUserFriendsList(loginUser, friendsList, friendId);

		// Update the friend's friends list
		User friendUser = userService.getById(friendId);
		List<Long> friendFriendsList = parseFriendsIds(friendUser.getFriendsIds());
		friendFriendsList.removeIf(id -> Objects.equals(id, loginUser.getId()));
		updateUserFriendsList(friendUser, friendFriendsList, loginUser.getId());

		return true;
	}

	private List<Long> parseFriendsIds(String friendsIds) {
		return Arrays.stream(friendsIds.substring(1, friendsIds.length() - 1).split(","))
				.map(String::trim)
				.map(Long::parseLong)
				.collect(Collectors.toList());
	}

	private void updateUserFriendsList(User user, List<Long> friendsList, Long friendId) {
		friendsList.removeIf(id -> Objects.equals(id, friendId));
		LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
		wrapper.eq(User::getId, user.getId()).set(User::getFriendsIds, friendsList.toString());
		userService.update(wrapper);
	}


	/**
	 * 获得好友申请
	 *
	 * @param loginUser
	 * @return
	 */
	@Override
	public List<FriendsRecordVO> obtainFriendApplicationRecords(User loginUser) {
		// 查询出当前用户所有申请、同意记录
		LambdaQueryWrapper<FriendApplication> friendApplicationLambdaQueryWrapper = new LambdaQueryWrapper<>();
		friendApplicationLambdaQueryWrapper.eq(FriendApplication::getReceiveId, loginUser.getId());
		List<FriendApplication> friendApplications = this.list(friendApplicationLambdaQueryWrapper);
		Collections.reverse(friendApplications);
		return friendApplications.stream().map(friend -> {
			FriendsRecordVO friendsRecordVO = new FriendsRecordVO();
			BeanUtils.copyProperties(friend, friendsRecordVO);
			User user = userService.getById(friend.getFromId());
			userService.cleanUser(user);
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(user, userVO);
			friendsRecordVO.setApplyUser(userVO);
			return friendsRecordVO;
		}).collect(Collectors.toList());
	}


	/**
	 * 获得我的申请
	 *
	 * @param loginUser
	 * @return
	 */
	@Override
	public List<FriendsRecordVO> obtainTheRecordOfMyApplication(User loginUser) {
		// 查询出当前用户所有申请、同意记录
		LambdaQueryWrapper<FriendApplication> myApplyLambdaQueryWrapper = new LambdaQueryWrapper<>();
		myApplyLambdaQueryWrapper.eq(FriendApplication::getFromId, loginUser.getId());
		List<FriendApplication> FriendApplicationList = this.list(myApplyLambdaQueryWrapper);
		Collections.reverse(FriendApplicationList);
		return FriendApplicationList.stream().map(friend -> {
			FriendsRecordVO friendsRecordVO = new FriendsRecordVO();


			//  todo 脱敏后的uservo 封装一下
			BeanUtils.copyProperties(friend, friendsRecordVO);
			User user = userService.getById(friend.getReceiveId());
			UserVO userVO = new UserVO();
			userService.cleanUser(user);
			BeanUtils.copyProperties(user, userVO);
			friendsRecordVO.setApplyUser(userVO);
			return friendsRecordVO;
		}).collect(Collectors.toList());
	}


	/**
	 * 未读记录数量
	 *
	 * @param loginUser
	 * @return
	 */
	@Override
	public int obtainTheNumberOfUnreadRecords(User loginUser) {
		LambdaQueryWrapper<FriendApplication> friendApplicationLambdaQueryWrapper = new LambdaQueryWrapper<>();
		friendApplicationLambdaQueryWrapper.eq(FriendApplication::getReceiveId, loginUser.getId());
		List<FriendApplication> friendApplications = this.list(friendApplicationLambdaQueryWrapper);
		int count = 0;
		for (FriendApplication friend : friendApplications) {
			if (friend.getStatus() == DEFAULT_STATUS && friend.getIsRead() == NOT_READ) {
				count++;
			}
		}
		return count;
	}


	/**
	 * 阅读记录
	 *
	 * @param loginUser
	 * @param ids
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	//todo 一条条看
	public boolean toRead(User loginUser, Set<Long> ids) {
		boolean flag = false;
		for (Long id : ids) {
			FriendApplication friend = this.getById(id);
			if (friend.getStatus() == DEFAULT_STATUS && friend.getIsRead() == NOT_READ) {
				friend.setIsRead(READ);
				flag = this.updateById(friend);
			}
		}
		return flag;
	}

	/**
	 * 统一申请
	 *
	 * @param loginUser
	 * @param fromId
	 * @return
	 */
	@Override
	public boolean agreeToApply(User loginUser, Long fromId) {
		// 0. 根据receiveId查询所有接收的申请记录
		LambdaQueryWrapper<FriendApplication> friendApplicationLambdaQueryWrapper = new LambdaQueryWrapper<>();
		friendApplicationLambdaQueryWrapper.eq(FriendApplication::getReceiveId, loginUser.getId());
		friendApplicationLambdaQueryWrapper.eq(FriendApplication::getFromId, fromId);
		List<FriendApplication> recordCount = this.list(friendApplicationLambdaQueryWrapper);
		List<FriendApplication> collect = recordCount.stream().filter(f -> f.getStatus() == DEFAULT_STATUS).collect(Collectors.toList());
		// 条数小于1 就不能再同意
		if (collect.isEmpty()) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "该申请不存在");
		}
		if (collect.size() > 1) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "操作有误,请重试");
		}
		AtomicBoolean flag = new AtomicBoolean(false);
		collect.forEach(friend -> {
			if (DateUtil.between(new Date(), friend.getCreateTime(), DateUnit.DAY) >= 3 || friend.getStatus() == EXPIRED_STATUS) {
				throw new ResultException(ErrorCode.PARAMS_ERROR, "该申请已过期");
			}
			// 1. 分别查询receiveId和fromId的用户，更改userIds中的数据
			User receiveUser = userService.getById(loginUser.getId());
			User fromUser = userService.getById(fromId);


			//todo user表添加firends字段
			// json 转集合添加内容后 再转json
			Set<Long> receiveUserIds = jsonToSet(receiveUser.getFriendsIds());
			Set<Long> fromUserUserIds = jsonToSet(fromUser.getFriendsIds());

			fromUserUserIds.add(receiveUser.getId());
			receiveUserIds.add(fromUser.getId());

			String jsonFromUserUserIds = gson.toJson(fromUserUserIds);
			String jsonReceiveUserIds = gson.toJson(receiveUserIds);
			receiveUser.setFriendsIds(jsonReceiveUserIds);
			fromUser.setFriendsIds(jsonFromUserUserIds);
			// 2. 修改状态由0改为1
			friend.setStatus(AGREE_STATUS);
			flag.set(userService.updateById(fromUser) && userService.updateById(receiveUser) && this.updateById(friend));
		});
		return flag.get();
	}

	/**
	 * 拒绝申请
	 *
	 * @param applyId   申请用户id
	 * @param loginUser 登录用户
	 * @return
	 */
	@Override
	public boolean canceledApply(Long applyId, User loginUser) {
		LambdaQueryWrapper<FriendApplication> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(FriendApplication::getFromId, applyId);
		FriendApplication friend = this.getOne(queryWrapper);
		if (friend.getStatus() != DEFAULT_STATUS) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "该申请已通过");
		}
		friend.setStatus(REVOKE_STATUS);
		return this.updateById(friend);
	}

	public static Set<Long> jsonToSet(String json) {
		Set<Long> set = new Gson().fromJson(json, new TypeToken<Set<Long>>() {
		}.getType());
		return Optional.ofNullable(set).orElse(new HashSet<>());
	}
}




