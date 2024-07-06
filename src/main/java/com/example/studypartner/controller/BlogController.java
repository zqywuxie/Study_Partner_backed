package com.example.studypartner.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.dto.BlogDTO;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.request.BlogAddRequest;
import com.example.studypartner.domain.request.BlogUpdateRequest;
import com.example.studypartner.domain.vo.BlogVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.manager.RedisLimiterManager;
import com.example.studypartner.service.BlogService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
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
	 * @param blogDTO
	 * @param request
	 * @return
	 */
	@PostMapping("/list")
	@ApiOperation("博客列表")
	public CommonResult<Page<BlogVO>> listBlogPage(@RequestBody BlogDTO blogDTO, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		return ResultUtils.success(blogService.pageBlog(blogDTO, loginUser.getId()));
	}


	/**
	 * @param blogAddRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/add")
	@ApiOperation("添加博客")
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
	 * 个人博文列表
	 *
	 * @param currentPage
	 * @param request
	 * @return
	 */
	@GetMapping("/list/my/blog")
	@ApiOperation("我的博客")
	public CommonResult<Page<BlogVO>> listMyBlogs(long currentPage, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		Page<BlogVO> blogPage = blogService.listMyBlogs(currentPage, loginUser.getId());
		return ResultUtils.success(blogPage);
	}

	/**
	 * 点赞博客
	 *
	 * @param id      id
	 * @param request 请求
	 * @return {@link CommonResult}<{@link String}>
	 */
	@PutMapping("/like/{id}")
	@ApiOperation("点赞博客")
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
	 * @param blogId  id
	 * @param request 请求
	 * @return {@link CommonResult}<{@link BlogVO}>
	 */
	@GetMapping("/{blogId}")
	@ApiOperation("通过id获取博客")
	public CommonResult<BlogVO> getBlogById(@PathVariable Long blogId, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
//        boolean contains = bloomFilter.contains(BLOG_BLOOM_PREFIX + id);
//        if (!contains){
//            return ResultUtils.success(null);
//        }
		if (blogId == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		return ResultUtils.success(blogService.getBlogById(blogId, loginUser.getId()));
	}

	/**
	 * 删除博客通过id
	 *
	 * @param id      id
	 * @param request 请求
	 * @return {@link CommonResult}<{@link String}>
	 */
	@DeleteMapping("/{id}")
	@ApiOperation("通过id删除博客")
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
	@ApiOperation("更新博客")
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
