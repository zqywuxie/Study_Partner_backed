package com.example.studypartner.controller;


import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.constant.RedisConstants;
import com.example.studypartner.domain.entity.Message;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息控制器
 *
 * @author Shier
 * @date 2023/06/22
 */
@RestController
@RequestMapping("/message")
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
	@GetMapping("/num/{type}")
	@ApiOperation(value = "获得消息数量")

	public CommonResult<Long> getUserMessageNum(@PathVariable("type") String type, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			return ResultUtils.success(0L);
		}
		long messageNum = messageService.getMessageNum(loginUser.getId(), Integer.valueOf(type));
		return ResultUtils.success(messageNum);
	}

	@GetMapping("/read/{type}")
	@ApiOperation(value = "消息已读")

	public CommonResult<Boolean> readMessage(@PathVariable("type") String type, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			return ResultUtils.failed(ErrorCode.NOT_LOGIN);
		}
		Boolean messageNum = messageService.readMessage(loginUser.getId(), Integer.valueOf(type));
		return ResultUtils.success(messageNum);
	}


	/**
	 * 获取用户博客消息
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link List}<{@link MessageVO}>>
	 */
	@GetMapping("/get/{type}")
	@ApiOperation(value = "根据信息类型获取通知消息")

	public CommonResult<List<MessageVO>> getUserBlogMessage(@PathVariable("type") String type, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		List<MessageVO> readMessage = messageService.getMessages(loginUser.getId(), Integer.valueOf(type));
		return ResultUtils.success(readMessage);
	}


	//	按时间顺序给出5条
	@GetMapping("/list")
	@ApiOperation(value = "获得所有通知信息")

	public CommonResult<List<MessageVO>> listMessage(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		List<MessageVO> messageVOList = messageService.getAllMessage(loginUser.getId());
		List<MessageVO> sortedList = messageVOList.stream().sorted(Comparator.comparing(MessageVO::getCreateTime).reversed()).limit(5).collect(Collectors.toList());
		return ResultUtils.success(sortedList);
	}

}
