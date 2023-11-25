package com.example.studypartner.controller;


import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.constant.RedisConstants;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.vo.BlogVO;
import com.example.studypartner.domain.vo.MessageVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.service.MessageService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 消息控制器
 *
 * @author Shier
 * @date 2023/06/22
 */
@RestController
@RequestMapping("/message")
@Api(tags = "消息管理模块")
public class MessageController {

	/**
	 * 消息服务
	 */
	@Resource
	private MessageService messageService;

	/**
	 * redis
	 */
	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private UserService userService;


	/**
	 * 用户是否有新消息
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link Boolean}>
	 */
	@GetMapping
	@ApiOperation(value = "用户是否有新消息")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "request", value = "request请求")})
	public CommonResult<Boolean> userHasNewMessage(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			return ResultUtils.success(false);
		}
		Boolean hasNewMessage = messageService.hasNewMessage(loginUser.getId());
		return ResultUtils.success(hasNewMessage);
	}

	/**
	 * 获取用户新消息数量
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link Long}>
	 */
	@GetMapping("/num")
	@ApiOperation(value = "获取用户新消息数量")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "request", value = "request请求")})
	public CommonResult<Long> getUserMessageNum(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			return ResultUtils.success(0L);
		}
		long messageNum = messageService.getMessageNum(loginUser.getId(), 0);
		return ResultUtils.success(messageNum);
	}

	/**
	 * 获取用户点赞消息数量
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link Long}>
	 */
	@GetMapping("/like/num")
	@ApiOperation(value = "获取用户点赞消息数量")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "request", value = "request请求")})
	public CommonResult<Long> getUserLikeMessageNum(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		long messageNum = messageService.getLikeNum(loginUser.getId());
		return ResultUtils.success(messageNum);
	}

	/**
	 * 获取用户点赞消息
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link List}<{@link MessageVO}>>
	 */
	@GetMapping("/like")
	@ApiOperation(value = "获取用户点赞消息")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "request", value = "request请求")})
	public CommonResult<List<MessageVO>> getUserLikeMessage(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		List<MessageVO> messageVOList = messageService.getLike(loginUser.getId());
		return ResultUtils.success(messageVOList);
	}

	/**
	 * 获取用户博客消息数量
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link String}>
	 */
	@GetMapping("/blog/num")
	@ApiOperation(value = "获取用户博客消息数量")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "request", value = "request请求")})
	public CommonResult<String> getUserBlogMessageNum(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		String likeNumKey = RedisConstants.MESSAGE_BLOG_NUM_KEY + loginUser.getId();
		Boolean hasKey = stringRedisTemplate.hasKey(likeNumKey);
		if (Boolean.TRUE.equals(hasKey)) {
			String num = stringRedisTemplate.opsForValue().get(likeNumKey);
			return ResultUtils.success(num);
		} else {
			return ResultUtils.success("0");
		}
	}

	/**
	 * 获取用户博客消息
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link List}<{@link BlogVO}>>
	 */
	@GetMapping("/blog")
	@ApiOperation(value = "获取用户博客消息")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "request", value = "request请求")})
	public CommonResult<List<BlogVO>> getUserBlogMessage(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		List<BlogVO> blogVOList = messageService.getUserBlog(loginUser.getId());
		return ResultUtils.success(blogVOList);
	}
}
