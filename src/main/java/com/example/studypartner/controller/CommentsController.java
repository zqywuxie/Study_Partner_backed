package com.example.studypartner.controller;

import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.request.CommentsAddRequest;
import com.example.studypartner.domain.vo.CommentsVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.manager.RedisLimiterManager;
import com.example.studypartner.service.CommentsService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 博文评论控制器
 *
 * @author wuxie
 * @date 2023/06/11
 */
@RestController
@RequestMapping("/comments")
public class CommentsController {
	/**
	 * 博客评论服务
	 */
	@Resource
	private CommentsService commentsService;

	/**
	 * 用户服务
	 */
	@Resource
	private UserService userService;

	@Resource
	private RedisLimiterManager redisLimiterManager;

	/**
	 * 添加评论
	 *
	 * @param commentsAddRequest 添加评论请求
	 * @param request            请求
	 * @return {@link CommonResult}<{@link String}>
	 */
	@PostMapping("/add")
	@ApiOperation(value = "添加评论")
	public CommonResult<String> addComment(@RequestBody CommentsAddRequest commentsAddRequest, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		// 限流次数太少了 todo
		boolean doRateLimit = redisLimiterManager.doRateLimit(loginUser.getId().toString());
		if (!doRateLimit) {
			throw new ResultException(ErrorCode.TOO_MANY_REQUEST);
		}
		if (commentsAddRequest.getBlogId() == null || StringUtils.isBlank(commentsAddRequest.getContent())) {
			throw new ResultException(ErrorCode.NULL_ERROR);
		}
		commentsService.addComment(commentsAddRequest, loginUser.getId());
		return ResultUtils.success("添加成功");
	}

	/**
	 * 博客评论列表
	 *
	 * @param blogId  博文id
	 * @param request 请求
	 * @return {@link CommonResult}<{@link List}<{@link CommentsVO}>>
	 */
	@GetMapping
	@ApiOperation(value = "根据id获取博文评论")
//	todo 优化page
	public CommonResult<List<CommentsVO>> listBlogComments(@RequestParam long blogId, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		List<CommentsVO> commentsVOS = commentsService.listComments(blogId, loginUser.getId());
		return ResultUtils.success(commentsVOS);
	}

	/**
	 * 喜欢评论
	 *
	 * @param id      id
	 * @param request 请求
	 * @return {@link CommonResult}<{@link String}>
	 */
	@PutMapping("/like/{id}")
	@ApiOperation(value = "点赞博文评论")
	public CommonResult<String> likeComment(@PathVariable Long id, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		commentsService.likeComment(id, loginUser.getId());
		return ResultUtils.success("ok");
	}

	/**
	 * 通过id获取评论
	 *
	 * @param id      id
	 * @param request 请求
	 * @return {@link CommonResult}<{@link CommentsVO}>
	 */
	@GetMapping("/{id}")
	@ApiOperation(value = "根据id获取评论")
	public CommonResult<CommentsVO> getCommentById(@PathVariable Long id, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		CommentsVO commentsVO = commentsService.getComment(id, loginUser.getId());
		return ResultUtils.success(commentsVO);
	}

	/**
	 * 删除博客评论
	 *
	 * @param id      id
	 * @param request 请求
	 * @return {@link CommonResult}<{@link String}>
	 */
	@DeleteMapping("/{id}")
	@ApiOperation(value = "根据id删除评论")
	public CommonResult<String> deleteBlogComment(@PathVariable Long id, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		commentsService.deleteComment(id, loginUser.getId());
		return ResultUtils.success("评论删除成功");
	}

	/**
	 * 获取我的评论
	 *
	 * @param request 请求
	 * @return {@link CommonResult}<{@link List}<{@link CommentsVO}>>
	 */
	@GetMapping("/list/my")
	@ApiOperation(value = "获取我的评论")
	public CommonResult<List<CommentsVO>> listMyBlogComments(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		List<CommentsVO> commentsVOList = commentsService.listMyComments(loginUser.getId());
		return ResultUtils.success(commentsVOList);
	}
}
