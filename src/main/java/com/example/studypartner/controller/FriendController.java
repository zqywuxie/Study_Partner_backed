package com.example.studypartner.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.request.FriendAddRequest;
import com.example.studypartner.domain.vo.FriendsRecordVO;
import com.example.studypartner.domain.vo.UserVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.service.FriendApplicationService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 好友控制器
 *
 * @author wuxie
 * @date 2023/06/19
 */
@RestController
@RequestMapping("/friends")
public class FriendController {
	/**
	 * 好友服务
	 */
	@Resource
	private FriendApplicationService friendApplicationService;

	/**
	 * 用户服务
	 */
	@Resource
	private UserService userService;

	/**
	 * 添加好友
	 *
	 * @param friendAddRequest 好友添加请求
	 * @param request          请求
	 * @return {@link CommonResult}<{@link Boolean}>
	 */
	@PostMapping("/add")
	@ApiOperation(value = "添加好友")

	public CommonResult<Boolean> addFriendRecords(@RequestBody FriendAddRequest friendAddRequest, HttpServletRequest request) {
		if (friendAddRequest == null) {
			throw new ResultException(ErrorCode.NULL_ERROR, "请求参数有误");
		}
		User loginUser = userService.getLoginUser(request);
		boolean addStatus = friendApplicationService.addFriendRecords(loginUser, friendAddRequest);
		return ResultUtils.success(addStatus);
	}


	/**
	 * 删除好友
	 *
	 * @param friendId 删除好友
	 * @param request  请求
	 * @return {@link CommonResult}<{@link Boolean}>
	 */
	@GetMapping("/delete/{friendId}")
	@ApiOperation(value = "删除好友")

		public CommonResult<Boolean> deleteFriendRecords(@PathVariable Long friendId, HttpServletRequest request) {
		if (friendId == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "请求参数为空");
		}
		User loginUser = userService.getLoginUser(request);
		boolean addStatus = friendApplicationService.deleteFriendRecords(loginUser, friendId);
		return ResultUtils.success(addStatus);
	}


	/**
	 * 查询记录
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link List}<{@link FriendsRecordVO}>>
	 */
	@GetMapping("/getRecords")
	@ApiOperation(value = "查询记录")

	public CommonResult<List<FriendsRecordVO>> getRecords(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		List<FriendsRecordVO> recordVOS = friendApplicationService.obtainFriendApplicationRecords(loginUser);
		return ResultUtils.success(recordVOS);
	}

	/**
	 * 获取未读记录条数
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link Integer}>
	 */
	@GetMapping("/getUnreadCount")
	@ApiOperation(value = "查询记录")

	public CommonResult<Integer> getRecordCount(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		int recordCount = friendApplicationService.obtainTheNumberOfUnreadRecords(loginUser);
		return ResultUtils.success(recordCount);
	}

	/**
	 * 获取我申请的记录
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link List}<{@link FriendsRecordVO}>>
	 */
	@GetMapping("/getMyRecords")
	@ApiOperation(value = "获取我申请的记录")

	public CommonResult<List<FriendsRecordVO>> getMyRecords(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		List<FriendsRecordVO> myFriendApplicationList = friendApplicationService.obtainTheRecordOfMyApplication(loginUser);
		return ResultUtils.success(myFriendApplicationList);
	}

	/**
	 * 按状态搜索好友列表
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link List}<{@link User}>>
	 */
	@GetMapping("/my/list")
	@ApiOperation(value = "通过用户名搜索用户")

	public CommonResult<List<UserVO>> getMyFriendList(HttpServletRequest request) {
		//todo redis存储的数据没有变动 后期优化
		User loginUser = userService.getById(userService.getLoginUser(request).getId());
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
//		Long loginUserId = loginUser.getId();
//		List<FriendApplication> friendApplications = friendApplicationService.list(new QueryWrapper<FriendApplication>()
//				.eq("receiveId", loginUserId)
//				.eq("status", 1));
//		// 使用流和Lambda表达式进行过滤查询
//		List<User> userList = friendApplications.stream().map(friendApplication -> userService.getById(friendApplication.getFromId())).collect(Collectors.toList());
		String friendsIds = loginUser.getFriendsIds();
		if (friendsIds == null) {
			return ResultUtils.failed(ErrorCode.NULL_ERROR, "无好友");
		}
		String[] idsArray = friendsIds.substring(1, friendsIds.length() - 1).split(",");
		// 将字符串数组转换为整数列表
		List<Integer> friendsList = Arrays.asList(idsArray).stream()
				.map(String::trim)
				.map(Integer::parseInt)
				.collect(Collectors.toList());
		LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
		wrapper.in(User::getId, friendsList);
		List<UserVO> userVOS = userService.list(wrapper).stream().map(user -> {
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(user, userVO);
			return userVO;
		}).collect(Collectors.toList());
		return ResultUtils.success(userVOS);
	}

	/**
	 * 同意申请
	 *
	 * @param fromId  从id
	 * @param request 请求
	 * @return {@link CommonResult}<{@link Boolean}>
	 */
	@PostMapping("/agree/{fromId}")
	@ApiOperation(value = "同意申请")

	public CommonResult<Boolean> agreeToApply(@PathVariable("fromId") Long fromId, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		boolean agreeToApplyStatus = friendApplicationService.agreeToApply(loginUser, fromId);
		return ResultUtils.success(agreeToApplyStatus);
	}

	/**
	 * 取消申请
	 *
	 * @param id      id
	 * @param request 请求
	 * @return {@link CommonResult}<{@link Boolean}>
	 */
	@PostMapping("/canceledApply/{id}")
	@ApiOperation(value = "取消申请")

	public CommonResult<Boolean> canceledApply(@PathVariable("id") Long id, HttpServletRequest request) {
		if (id == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "请求有误");
		}
		User loginUser = userService.getLoginUser(request);
		boolean canceledApplyStatus = friendApplicationService.canceledApply(id, loginUser);
		return ResultUtils.success(canceledApplyStatus);
	}

	/**
	 * 阅读
	 *
	 * @param ids     id
	 * @param request 请求
	 * @return {@link CommonResult}<{@link Boolean}>
	 */
	@GetMapping("/read")
	@ApiOperation(value = "阅读")

	public CommonResult<Boolean> toRead(@RequestParam(required = false) Set<Long> ids, HttpServletRequest request) {
		if (CollectionUtils.isEmpty(ids)) {
			return ResultUtils.success(false);
		}
		User loginUser = userService.getLoginUser(request);
		boolean isRead = friendApplicationService.toRead(loginUser, ids);
		return ResultUtils.success(isRead);
	}
}
