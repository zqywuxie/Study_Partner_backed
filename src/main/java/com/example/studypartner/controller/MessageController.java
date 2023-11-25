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
import org.springframework.web.bind.annotation.*;

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
	 * 获取用户新消息数量
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link Long}>
	 */
	@GetMapping("/num")
	@ApiOperation(value = "获得消息数量")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "type", value = "数据类型"), @ApiImplicitParam(name = "request", value = "request请求")})
	public CommonResult<Long> getUserMessageNum(@RequestParam String type, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			return ResultUtils.success(0L);
		}
		long messageNum = messageService.getMessageNum(loginUser.getId(), Integer.valueOf(type));
		return ResultUtils.success(messageNum);
	}


	/**
	 * 获取用户博客消息
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link List}<{@link MessageVO}>>
	 */
	@GetMapping("/get")
	@ApiOperation(value = "获取用户博客消息")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "type", value = "消息类型"),
					@ApiImplicitParam(name = "request", value = "request请求")})
	public CommonResult<List<MessageVO>> getUserBlogMessage(@RequestParam String type, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		List<MessageVO> blogVOList = messageService.getMessages(loginUser.getId(), Integer.valueOf(type));
		return ResultUtils.success(blogVOList);
	}
}
