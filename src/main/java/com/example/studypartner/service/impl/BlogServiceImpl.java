package com.example.studypartner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.dto.BlogDTO;
import com.example.studypartner.domain.entity.*;
import com.example.studypartner.domain.enums.MessageTypeEnum;
import com.example.studypartner.domain.request.BlogAddRequest;
import com.example.studypartner.domain.request.BlogUpdateRequest;
import com.example.studypartner.domain.vo.BlogVO;
import com.example.studypartner.domain.vo.UserVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.mapper.BlogMapper;
import com.example.studypartner.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.studypartner.constant.RedisConstants.*;
import static com.example.studypartner.constant.SystemConstant.DEFAULT_PAGE_SIZE;


/**
 * @author wuxie
 * @description 针对表【blog】的数据库操作Service实现
 * @createDate 2023-11-03 15:54:34
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

	@Resource
	private BloglikeService blogLikeService;

	@Resource
	private UserService userService;

	@Resource
	private FollowService followService;

	@Resource
	private OssService fileService;

	@Resource
	private MessageService messageService;

	@Resource
	private RedisTemplate stringRedisTemplate;


	@Override
	public Page<BlogVO> listMyBlogs(long currentPage, Long userId) {
		if (currentPage <= 0) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}

		Page<Blog> blogPage = this.page(new Page<>(currentPage, DEFAULT_PAGE_SIZE), new LambdaQueryWrapper<Blog>().eq(Blog::getUserId, userId));

		List<BlogVO> blogVOList = blogPage.getRecords().stream().map(blog -> {
			BlogVO blogVO = new BlogVO();
			BeanUtils.copyProperties(blog, blogVO);
			return blogVO;
		}).peek(blogVO -> {
			UserVO userVO = getUserVO(blogVO.getUserId());
			blogVO.setAuthor(userVO);
		}).peek(blogVO -> {
			String images = blogVO.getImages();
			if (images != null) {
				String[] imgStr = images.split(",");
				blogVO.setCoverImage(imgStr[0]);
			}
		}).collect(Collectors.toList());

		Page<BlogVO> blogVoPage = new Page<>();
		BeanUtils.copyProperties(blogPage, blogVoPage);
		blogVoPage.setRecords(blogVOList);
		return blogVoPage;
	}

	private UserVO getUserVO(Long userId) {
		User user = userService.getById(userId);
		UserVO userVO = new UserVO();
		BeanUtils.copyProperties(user, userVO);
		return userVO;
	}


	//region 点赞博客
	@Override
	public void likeBlog(long blogId, Long userId) {
		// todo redis实现
		// todo 分布式锁

		Blog blog = this.getById(blogId);

		if (blog == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "博文不存在");
		}

		String key = LIKE_COUNT_KEY + userId;

		LambdaQueryWrapper<BlogLike> blogLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
		blogLikeLambdaQueryWrapper.eq(BlogLike::getUserId, userId).eq(BlogLike::getBlogId, blogId);

		long isLike = blogLikeService.count(blogLikeLambdaQueryWrapper);

		if (isLike > 0) {
			handleUnlike(blog, blogId, userId, key);
		} else {
			handleLike(blog, blogId, userId, key);
		}
	}

	private void handleUnlike(Blog blog, long blogId, Long userId, String key) {
		blogLikeService.remove(new LambdaQueryWrapper<BlogLike>().eq(BlogLike::getUserId, userId).eq(BlogLike::getBlogId, blogId));

		int newNum = blog.getLikedNum() - 1;
		this.update().eq("id", blogId).set("liked_num", newNum).update();
		stringRedisTemplate.opsForValue().decrement(key);
	}

	private void handleLike(Blog blog, long blogId, Long userId, String key) {
		BlogLike blogLike = new BlogLike();
		blogLike.setBlogId(blogId);
		blogLike.setUserId(userId);
		blogLikeService.save(blogLike);

		int newNum = blog.getLikedNum() + 1;
		this.update().eq("id", blogId).set("liked_num", newNum).update();
		stringRedisTemplate.opsForValue().increment(key);

		// todo 添加点赞消息
		addLikeMessage(userId, blog);

		// 更新被点赞用户的消息计数
		updateLikeNumKey(blog.getUserId());
	}

	private void addLikeMessage(Long userId, Blog blog) {
		Message message = new Message();
		message.setType(MessageTypeEnum.BLOG_LIKE.getValue());
		message.setFromId(userId);
		message.setToId(blog.getUserId());
		message.setData(String.valueOf(blog.getId()));
		messageService.save(message);
	}

	// 更新点赞缓存
	private void updateLikeNumKey(Long blogUserId) {
		String likeNumKey = MESSAGE_LIKE_NUM_KEY + blogUserId;
		Boolean hasKey = stringRedisTemplate.hasKey(likeNumKey);

		if (Boolean.TRUE.equals(hasKey)) {
			stringRedisTemplate.opsForValue().increment(likeNumKey);
		} else {
			stringRedisTemplate.opsForValue().set(likeNumKey, 1);
		}
	}


	//endregion


	// region 博客分页
	@Override
	public Page<BlogVO> pageBlog(BlogDTO blogDTO, Long userId) {
		int pageSize = blogDTO.getPageSize();
		int pageNum = blogDTO.getPageNum();
		String searchText = blogDTO.getSearchText();


		if (pageNum <= 0) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}

		LambdaQueryWrapper<Blog> blogLambdaQueryWrapper = new LambdaQueryWrapper<>();
		blogLambdaQueryWrapper.orderByDesc(Blog::getCreateTime);
		//		关键字查询
		blogLambdaQueryWrapper.like(Blog::getContent, searchText).or(b -> b.like(Blog::getTitle, searchText));
		Page<Blog> blogPage = this.page(new Page<>(pageNum, pageSize), blogLambdaQueryWrapper);

		List<BlogVO> blogVOList = blogPage.getRecords().stream().map(blog -> {
			BlogVO blogVO = new BlogVO();
			BeanUtils.copyProperties(blog, blogVO);
			return blogVO;
		}).peek(blogVO -> {
			UserVO userVO = getBlogAuthor(blogVO.getUserId());
			blogVO.setAuthor(userVO);
		}).peek(blogVO -> {
			long count = getBlogLikeCount(blogVO.getId(), userId);
			blogVO.setIsLike(count > 0);
		}).collect(Collectors.toList());

		blogVOList.forEach(this::processBlogImages);

		Page<BlogVO> blogVoPage = new Page<>();
		BeanUtils.copyProperties(blogPage, blogVoPage);
		blogVoPage.setRecords(blogVOList);

		return blogVoPage;
	}

	private UserVO getBlogAuthor(Long userId) {
		LambdaQueryWrapper<User> blogAuthorQuery = new LambdaQueryWrapper<>();
		blogAuthorQuery.eq(User::getId, userId);
		User author = userService.getOne(blogAuthorQuery);

		if (author == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "博客没有作者");
		}

		UserVO userVO = new UserVO();
		BeanUtils.copyProperties(author, userVO);
		return userVO;
	}

	private long getBlogLikeCount(long blogId, Long userId) {
		LambdaQueryWrapper<BlogLike> blogLikeQuery = new LambdaQueryWrapper<>();
		blogLikeQuery.eq(BlogLike::getBlogId, blogId).eq(BlogLike::getUserId, userId);
		return blogLikeService.count(blogLikeQuery);
	}


	//endregion


	//region 根据id获得博文
	@Override
	public BlogVO getBlogById(long blogId) {
		Blog blog = this.getById(blogId);
		BlogVO blogVO = new BlogVO();
		BeanUtils.copyProperties(blog, blogVO);
		String images = blogVO.getImages();
		if (images == null) {
			return blogVO;
		}
		String[] imgStrs = images.split(",");
		ArrayList<String> imgStrList = new ArrayList<>();
		for (String imgStr : imgStrs) {
			imgStrList.add(imgStr);
		}
		String imgStr = StringUtils.join(imgStrList, ",");
		blogVO.setImages(imgStr);
		blogVO.setCoverImage(imgStrList.get(0));
		return blogVO;
	}

	@Override
	public BlogVO getBlogById(long blogId, Long userId) {
		Blog blog = this.getById(blogId);
		if (blog == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "博文不存在");
		}

		BlogVO blogVO = new BlogVO();
		BeanUtils.copyProperties(blog, blogVO);

		long likeCount = getBlogLikeCount(blogId, userId);
		blogVO.setIsLike(likeCount > 0);

		UserVO authorVO = getBlogAuthorAndFollowStatus(blog.getUserId(), userId);
		blogVO.setAuthor(authorVO);

		processBlogImages(blogVO);

		return blogVO;
	}


	private UserVO getBlogAuthorAndFollowStatus(Long authorId, Long userId) {
		User author = userService.getById(authorId);
		if (author == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "博客没有作者");
		}

		UserVO authorVO = new UserVO();
		BeanUtils.copyProperties(author, authorVO);

		LambdaQueryWrapper<Follow> followQuery = new LambdaQueryWrapper<>();
		followQuery.eq(Follow::getFollowUserId, authorVO.getId()).eq(Follow::getUserId, userId);

		long followCount = followService.count(followQuery);
		authorVO.setIsFollow(followCount > 0);

		return authorVO;
	}


	//endregion


	// 处理博文的封面
	private void processBlogImages(BlogVO blogVO) {
		String images = blogVO.getImages();
		if (images != null) {
			String[] imgStrs = images.split(",");
			ArrayList<String> imgStrList = new ArrayList<>(Arrays.asList(imgStrs));
			String imgStr = StringUtils.join(imgStrList, ",");
			blogVO.setImages(imgStr);
			blogVO.setCoverImage(imgStrList.get(0));
		}
	}


	// region 博客增删改查
	@Override
	public void deleteBlog(Long blogId, Long userId, boolean isAdmin) {
		if (isAdmin) {
			this.removeById(blogId);
			return;
		}
		Blog blog = this.getById(blogId);
		if (!userId.equals(blog.getUserId())) {
			throw new ResultException(ErrorCode.NOT_ADMIN);
		}
		this.removeById(blogId);
	}

	@Override
	public void updateBlog(BlogUpdateRequest blogUpdateRequest, Long userId, boolean isAdmin) {
		if (blogUpdateRequest.getId() == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		Long createUserId = this.getById(blogUpdateRequest.getId()).getUserId();
		if (!createUserId.equals(userId) && !isAdmin) {
			throw new ResultException(ErrorCode.NOT_ADMIN, "没有权限");
		}
		String title = blogUpdateRequest.getTitle();
		String content = blogUpdateRequest.getContent();
		if (StringUtils.isAnyBlank(title, content)) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		Blog blog = new Blog();
		blog.setId(blogUpdateRequest.getId());
		ArrayList<String> imageNameList = new ArrayList<>();
		if (StringUtils.isNotBlank(blogUpdateRequest.getImgStr())) {
			String imgStr = blogUpdateRequest.getImgStr();
			String[] imgs = imgStr.split(",");
			for (String img : imgs) {
				imageNameList.add(img.substring(25));
			}
		}
		if (blogUpdateRequest.getImages() != null) {
			MultipartFile[] images = blogUpdateRequest.getImages();
			for (MultipartFile image : images) {
				// 上传到阿里云
				String filename = fileService.uploadFileAvatar(image);

				imageNameList.add(filename);
			}
		}
		if (imageNameList.size() > 0) {
			String imageStr = StringUtils.join(imageNameList, ",");
			blog.setImages(imageStr);
		}
		blog.setTitle(blogUpdateRequest.getTitle());
		blog.setContent(blogUpdateRequest.getContent());
		this.updateById(blog);
	}


	@Override
	public Long addBlog(BlogAddRequest blogAddRequest, User loginUser) {
		Blog blog = new Blog();
		ArrayList<String> imageNameList = new ArrayList<>();
		try {
			MultipartFile[] images = blogAddRequest.getImages();
			if (images != null) {
				for (MultipartFile image : images) {
					// 上传到阿里云
					String filename = fileService.uploadFileAvatar(image);
					imageNameList.add(filename);
				}
				String imageStr = StringUtils.join(imageNameList, ",");
				blog.setImages(imageStr);
			}
		} catch (Exception e) {
			throw new ResultException(ErrorCode.SYSTEM_ERROR, e.getMessage());
		}
		blog.setUserId(loginUser.getId());
		blog.setTitle(blogAddRequest.getTitle());
		blog.setContent(blogAddRequest.getContent());
		boolean saved = this.save(blog);
		if (saved) {
			List<UserVO> userVOList = followService.listFans(loginUser.getId());
			if (!userVOList.isEmpty()) {
				for (UserVO userVO : userVOList) {
					String key = BLOG_FEED_KEY + userVO.getId();
					stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
					String likeNumKey = MESSAGE_BLOG_NUM_KEY + userVO.getId();
					Boolean hasKey = stringRedisTemplate.hasKey(likeNumKey);
					if (Boolean.TRUE.equals(hasKey)) {
						stringRedisTemplate.opsForValue().increment(likeNumKey);
					} else {
						stringRedisTemplate.opsForValue().set(likeNumKey, "1");
					}
				}
			}
		}
		return blog.getId();
	}

	//endregion
}




