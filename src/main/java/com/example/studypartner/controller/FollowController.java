package com.example.studypartner.controller;


import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.vo.UserVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.manager.RedisLimiterManager;
import com.example.studypartner.service.FollowService;
import com.example.studypartner.service.MessageService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 关注控制器
 *
 * @author wuxie
 * @date 2023/06/11
 */
@RestController
@RequestMapping("/follow")
public class FollowController {
	/**
	 * 关注服务
	 */
	@Resource
	private FollowService followService;

	@Resource
	private MessageService messageService;

	@Resource
	private UserService userService;

	@Resource
	private RedisLimiterManager limiterManager;


	@Resource
	private RedisTemplate redisTemplate;

	/**
	 * 关注用户
	 *
	 * @param id      id
	 * @param request 请求
	 * @return {@link CommonResult}<{@link String}>
	 */
	@PostMapping("/{followerId}")
	@ApiOperation(value = "关注用户")

	public CommonResult<String> followUser(@PathVariable Long followerId, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN, "未登录");
		}
		Long loginUserId = loginUser.getId();
		// 限流
		boolean rateLimit = limiterManager.doRateLimit(loginUserId.toString());
		if (!rateLimit) {
			throw new ResultException(ErrorCode.TOO_MANY_REQUEST, "请求过于频繁");
		}
		followService.followUser(loginUserId, followerId);


		return ResultUtils.success("ok");
	}

	/**
	 * 获得我的粉丝
	 *
	 * @param request
	 * @return {@link CommonResult}<{@link String}>
	 */

	@GetMapping("/fans")
	@ApiOperation(value = "获取粉丝")

	public CommonResult<List<UserVO>> listFans(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		List<UserVO> userVOList = followService.listFans(loginUser.getId());
		return ResultUtils.success(userVOList);
	}

	@GetMapping("/fansCount")
	@ApiOperation(value = "获取粉丝数量")

	public CommonResult<Integer> fansCount(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		return ResultUtils.success(followService.fansCount(loginUser.getId()));
	}

	@GetMapping("/myCount")
	@ApiOperation(value = "我关注数")

	public CommonResult<Integer> myFollowCount(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		return ResultUtils.success(followService.myFollowCount(loginUser.getId()));
	}

	/**
	 * 获取我关注的用户
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link List}<{@link UserVO}>>
	 */
	@GetMapping("/my")
	@ApiOperation(value = "获取我关注的用户")

	public CommonResult<List<UserVO>> listMyFollow(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NULL_ERROR);
		}
		List<UserVO> userVOList = followService.listMyFollow(loginUser.getId());
		return ResultUtils.success(userVOList);
	}
}
