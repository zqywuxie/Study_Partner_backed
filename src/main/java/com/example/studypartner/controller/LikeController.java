
package com.example.studypartner.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.*;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.service.*;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.studypartner.constant.RedisConstants.LIKE_COUNT_KEY;


/**
 * @author wuxie
 * @CreateDate 2023/06/27 8:50
 * @description
 */
@RestController
@RequestMapping("/like")
@Slf4j
public class LikeController {


	@Resource
	private BloglikeService bloglikeService;

	@Resource
	private CommentLikeService commentLikeService;
	@Resource
	private CommentsService commentsService;
	@Resource
	private BlogService blogService;

	@Resource
	private UserService userService;

	@Resource
	private RedisTemplate redisTemplate;


	@GetMapping("/count")
	@ApiOperation(value = "点赞数")

	public CommonResult<Integer> getLikeCount(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		String key = LIKE_COUNT_KEY + loginUser.getId();
		ValueOperations valueOperations = redisTemplate.opsForValue();

		if (redisTemplate.hasKey(key)) {
			Integer cachedLikeCount = (Integer) valueOperations.get(key);
			return ResultUtils.success(cachedLikeCount);
		}

		// If data is not found in cache, query the database

		// Calculate the total count
		long totalCount = getBlogAndCommentLikeCount(loginUser.getId());

		redisTemplate.opsForValue().set(key, totalCount);
		redisTemplate.expire(key, 1, TimeUnit.HOURS);
		return ResultUtils.success(Math.toIntExact(totalCount));
	}

	private Long getBlogAndCommentLikeCount(Long userId) {
		List<Long> blogIds = blogService.lambdaQuery()
				.eq(Blog::getUserId, userId)
				.list()
				.stream()
				.map(Blog::getId)
				.collect(Collectors.toList());
		List<Long> commentsIds = commentsService.lambdaQuery()
				.eq(Comments::getUserId, userId)
				.list()
				.stream()
				.map(Comments::getId)
				.collect(Collectors.toList());

		if (blogIds.isEmpty() && commentsIds.isEmpty()) {
			// Handle the case when the list is empty (e.g., return a default value or throw an exception)
			return 0L;
		}

		LambdaQueryWrapper<BlogLike> blogLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
		blogLikeLambdaQueryWrapper.in(BlogLike::getBlogId, blogIds);


		LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
		commentLikeLambdaQueryWrapper.in(CommentLike::getCommentId,
				blogIds);
		return commentLikeService.count(commentLikeLambdaQueryWrapper) + bloglikeService.count(blogLikeLambdaQueryWrapper);
	}


}
