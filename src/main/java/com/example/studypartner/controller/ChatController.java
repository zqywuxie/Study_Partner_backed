
package com.example.studypartner.controller;

import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.constant.ChatConstant;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.request.ChatRequest;
import com.example.studypartner.domain.vo.ChatMessageVO;
import com.example.studypartner.domain.vo.UserVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.service.ChatService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 聊天控制器
 *
 * @author wuxie
 * @date 2023/06/19
 */
@RestController
@RequestMapping("/chat")
public class ChatController {
	/**
	 * 聊天服务
	 */
	@Resource
	private ChatService chatService;

	/**
	 * 用户服务
	 */
	@Resource
	private UserService userService;

	/**
	 * 私聊
	 *
	 * @param chatRequest 聊天请求
	 * @param request     请求
	 * @return {@link CommonResult}<{@link List}<{@link ChatMessageVO}>>
	 */
	@PostMapping("/privateChat")
	@ApiOperation(value = "获取私聊")
	public CommonResult<List<ChatMessageVO>> getPrivateChat(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
		if (chatRequest == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		List<ChatMessageVO> privateChat = chatService.getPrivateChat(chatRequest, ChatConstant.PRIVATE_CHAT, loginUser);
		return ResultUtils.success(privateChat);
	}


	/**
	 * 获得私聊的伙伴
	 *
	 * @param chatRequest
	 * @param request
	 * @return
	 */

	@GetMapping("/privateUser")
	@ApiOperation(value = "获取私聊用户")
	public CommonResult<List<UserVO>> getPrivateUser(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		List<UserVO> privateChat = chatService.getPrivateUser(loginUser);
		return ResultUtils.success(privateChat);
	}

	/**
	 * 团队聊天
	 *
	 * @param chatRequest 聊天请求
	 * @param request     请求
	 * @return {@link CommonResult}<{@link List}<{@link ChatMessageVO}>>
	 */
	@PostMapping("/teamChat")
	@ApiOperation(value = "获取队伍聊天")
	public CommonResult<List<ChatMessageVO>> getTeamChat(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
		if (chatRequest == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "请求有误");
		}
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		List<ChatMessageVO> teamChat = chatService.getTeamChat(chatRequest, ChatConstant.TEAM_CHAT, loginUser);
		return ResultUtils.success(teamChat);
	}

	/**
	 * 大厅聊天
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link List}<{@link ChatMessageVO}>>
	 */
	@GetMapping("/hallChat")
	@ApiOperation(value = "获取大厅聊天")
	public CommonResult<List<ChatMessageVO>> getHallChat(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		List<ChatMessageVO> hallChat = chatService.getHallChat(ChatConstant.HALL_CHAT, loginUser);
		return ResultUtils.success(hallChat);
	}


}