package com.example.studypartner.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.request.BlogAddRequest;
import com.example.studypartner.domain.request.BlogUpdateRequest;
import com.example.studypartner.domain.vo.BlogVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.manager.RedisLimiterManager;
import com.example.studypartner.service.BlogService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 博客接口
 *
 * @author wuxie
 * @date 2023/06/11
 */
@RestController
@RequestMapping("/blog")
@Api(tags = "博文管理模块")
public class BlogController {
	/**
	 * 博客服务
	 */
	@Resource
	private BlogService blogService;

	/**
	 * 用户服务
	 */
	@Resource
	private UserService userService;

	@Resource
	private RedisLimiterManager redisLimiterManager;

//    /**
//     * 布隆过滤器
//     */
//    @Resource
//    private BloomFilter bloomFilter;

	/**
	 * 博客列表
	 *
	 * @param currentPage
	 * @param request
	 * @return
	 */

	@GetMapping("/list")
	@ApiOperation(value = "获取博文")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "currentPage", value = "当前页"),
					@ApiImplicitParam(name = "request", value = "request请求")})
	public CommonResult<Page<BlogVO>> listBlogPage(long currentPage, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		return ResultUtils.success(blogService.pageBlog(currentPage, loginUser.getId()));
	}

	/**
	 * 添加博客
	 *
	 * @param blogAddRequest 博客添加请求
	 * @param request        请求
	 * @return {@link BaseResponse}<{@link String}>
	 */
	@PostMapping("/add")
	@ApiOperation(value = "添加博文")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "blogAddRequest", value = "博文添加请求"),
					@ApiImplicitParam(name = "request", value = "request请求")})
	public CommonResult<String> addBlog(BlogAddRequest blogAddRequest, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		// 限流
		boolean doRateLimit = redisLimiterManager.doRateLimit(loginUser.getId().toString());
		if (!doRateLimit) {
			throw new ResultException(ErrorCode.TOO_MANY_REQUEST);
		}
		if (StringUtils.isAnyBlank(blogAddRequest.getTitle(), blogAddRequest.getContent())) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "内容不能为空");
		}
		blogService.addBlog(blogAddRequest, loginUser);
//        bloomFilter.add(BLOG_BLOOM_PREFIX + blogId);
		return ResultUtils.success("添加成功");
	}

	/**
	 * 我博客列表
	 *
	 * @param currentPage 当前页面
	 * @param request     请求
	 * @return {@link BaseResponse}<{@link Page}<{@link BlogVO}>>
	 */
	@GetMapping("/list/my/blog")
	@ApiOperation(value = "获取我写的博文")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "currentPage", value = "当前页"),
					@ApiImplicitParam(name = "request", value = "request请求")})
	public CommonResult<Page<BlogVO>> listMyBlogs(long currentPage, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		Page<BlogVO> blogPage = blogService.listMyBlogs(currentPage, loginUser.getId());
		return ResultUtils.success(blogPage);
	}

	/**
	 * 像博客
	 *
	 * @param id      id
	 * @param request 请求
	 * @return {@link BaseResponse}<{@link String}>
	 */
	@PutMapping("/like/{id}")
	@ApiOperation(value = "点赞博文")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "id", value = "博文id"),
					@ApiImplicitParam(name = "request", value = "request请求")})
	public CommonResult<String> likeBlog(@PathVariable long id, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		blogService.likeBlog(id, loginUser.getId());
		return ResultUtils.success("成功");
	}

	/**
	 * 通过id获取博客
	 *
	 * @param id      id
	 * @param request 请求
	 * @return {@link CommonResult}<{@link BlogVO}>
	 */
	@GetMapping("/{id}")
	@ApiOperation(value = "根据id获取博文")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "id", value = "博文id"),
					@ApiImplicitParam(name = "request", value = "request请求")})
	public CommonResult<BlogVO> getBlogById(@PathVariable Long id, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
//        boolean contains = bloomFilter.contains(BLOG_BLOOM_PREFIX + id);
//        if (!contains){
//            return ResultUtils.success(null);
//        }
		if (id == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		return ResultUtils.success(blogService.getBlogById(id, loginUser.getId()));
	}

	/**
	 * 删除博客通过id
	 *
	 * @param id      id
	 * @param request 请求
	 * @return {@link CommonResult}<{@link String}>
	 */
	@DeleteMapping("/{id}")
	@ApiOperation(value = "根据id删除博文")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "id", value = "博文id"),
					@ApiImplicitParam(name = "request", value = "request请求")})
	public CommonResult<String> deleteBlogById(@PathVariable Long id, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		if (id == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		boolean admin = userService.isAdmin(loginUser);
		blogService.deleteBlog(id, loginUser.getId(), admin);
		return ResultUtils.success("删除成功");
	}

	/**
	 * 更新博客
	 *
	 * @param blogUpdateRequest 博客更新请求
	 * @param request           请求
	 * @return {@link CommonResult}<{@link String}>
	 */
	@PutMapping("/update")
	@ApiOperation(value = "更新博文")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "blogUpdateRequest", value = "博文更新请求"),
					@ApiImplicitParam(name = "request", value = "request请求")})
	public CommonResult<String> updateBlog(BlogUpdateRequest blogUpdateRequest, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		boolean admin = userService.isAdmin(loginUser);
		blogService.updateBlog(blogUpdateRequest, loginUser.getId(), admin);
		return ResultUtils.success("更新成功");
	}
}
