package com.example.studypartner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.studypartner.common.ErrorCode;
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
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.studypartner.constant.RedisConstants.*;
import static com.example.studypartner.constant.SystemConstant.PAGE_SIZE;


/**
 * @author wuxie
 * @description 针对表【blog】的数据库操作Service实现
 * @createDate 2023-06-03 15:54:34
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
		implements BlogService {

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

	@Override
	public Page<BlogVO> listMyBlogs(long currentPage, Long id) {
		if (currentPage <= 0) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		LambdaQueryWrapper<Blog> blogLambdaQueryWrapper = new LambdaQueryWrapper<>();
		blogLambdaQueryWrapper.eq(Blog::getUserId, id);
		Page<Blog> blogPage = this.page(new Page<>(currentPage, PAGE_SIZE), blogLambdaQueryWrapper);
		Page<BlogVO> blogVoPage = new Page<>();
		BeanUtils.copyProperties(blogPage, blogVoPage);
		List<BlogVO> blogVOList = blogPage.getRecords().stream().map((blog) -> {
			BlogVO blogVO = new BlogVO();
			BeanUtils.copyProperties(blog, blogVO);
			return blogVO;
		}).collect(Collectors.toList());
		for (BlogVO blogVO : blogVOList) {
			User user = userService.getById(blogVO.getUserId());
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(user, userVO);
			String images = blogVO.getImages();
			if (images == null) {
				continue;
			}
			String[] imgStr = images.split(",");
			blogVO.setCoverImage(imgStr[0]);
			blogVO.setAuthor(userVO);
		}
		blogVoPage.setRecords(blogVOList);
		return blogVoPage;
	}

	@Override
	public void likeBlog(long blogId, Long userId) {
		// todo redis实现
		// todo 分布式锁
		Blog blog = this.getById(blogId);
		String key = LIKE_COUNT_KEY + userId;

		if (blog == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "博文不存在");
		}
		LambdaQueryWrapper<BlogLike> blogLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
		blogLikeLambdaQueryWrapper.eq(BlogLike::getUserId, userId).eq(BlogLike::getBlogId, blogId);
		long isLike = blogLikeService.count(blogLikeLambdaQueryWrapper);
		if (isLike > 0) {
			blogLikeService.remove(blogLikeLambdaQueryWrapper);
			int newNum = blog.getLikedNum() - 1;
			this.update().eq("id", blogId).set("likedNum", newNum).update();
			stringRedisTemplate.opsForValue().decrement(key);

		} else {
			BlogLike blogLike = new BlogLike();
			blogLike.setBlogId(blogId);
			blogLike.setUserId(userId);
			blogLikeService.save(blogLike);
			int newNum = blog.getLikedNum() + 1;
			this.update().eq("id", blogId).set("likedNum", newNum).update();
			stringRedisTemplate.opsForValue().increment(key);

			// todo 添加点赞消息
			Message message = new Message();
			message.setType(MessageTypeEnum.BLOG_LIKE.getValue());
			message.setFromId(userId);
			message.setToId(blog.getUserId());
			message.setData(String.valueOf(blog.getId()));
			messageService.save(message);
			String likeNumKey = MESSAGE_LIKE_NUM_KEY + blog.getUserId();
			Boolean hasKey = stringRedisTemplate.hasKey(likeNumKey);
			if (Boolean.TRUE.equals(hasKey)) {
				stringRedisTemplate.opsForValue().increment(likeNumKey);
			} else {
				stringRedisTemplate.opsForValue().set(likeNumKey, "1");
			}
		}
	}


	@Override
	public Page<BlogVO> pageBlog(long currentPage, Long userId) {
		LambdaQueryWrapper<Blog> blogLambdaQueryWrapper = new LambdaQueryWrapper<>();
		blogLambdaQueryWrapper.orderBy(true, false, Blog::getCreateTime);
		Page<Blog> blogPage = this.page(new Page<>(currentPage, PAGE_SIZE), blogLambdaQueryWrapper);
		Page<BlogVO> blogVoPage = new Page<>();
		BeanUtils.copyProperties(blogPage, blogVoPage);
		List<BlogVO> blogVOList = blogPage.getRecords().stream().map((blog) -> {
			BlogVO blogVO = new BlogVO();
			BeanUtils.copyProperties(blog, blogVO);
			LambdaQueryWrapper<BlogLike> blogLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
			LambdaQueryWrapper<User> blogAuthor = new LambdaQueryWrapper<>();
			blogAuthor.eq(User::getId, blog.getUserId());
			// todo 优化代码
			User author = userService.getOne(blogAuthor);
			if (author == null) {
				throw new ResultException(ErrorCode.PARAMS_ERROR, "博客没有作者");
			}
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(author, userVO);
			blogLikeLambdaQueryWrapper.eq(BlogLike::getBlogId, blog.getId()).eq(BlogLike::getUserId, userId);
			long count = blogLikeService.count(blogLikeLambdaQueryWrapper);
			blogVO.setAuthor(userVO);
			blogVO.setIsLike(count > 0);
			return blogVO;
		}).collect(Collectors.toList());
		for (BlogVO blogVO : blogVOList) {
			String images = blogVO.getImages();
			if (images == null) {
				continue;
			}
			String[] imgStrs = images.split(",");
			blogVO.setCoverImage(imgStrs[0]);
		}
		blogVoPage.setRecords(blogVOList);
		return blogVoPage;
	}


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
		BlogVO blogVO = new BlogVO();
		BeanUtils.copyProperties(blog, blogVO);
		LambdaQueryWrapper<BlogLike> blogLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
		blogLikeLambdaQueryWrapper.eq(BlogLike::getUserId, userId);
		blogLikeLambdaQueryWrapper.eq(BlogLike::getBlogId, blogId);
		long isLike = blogLikeService.count(blogLikeLambdaQueryWrapper);
		blogVO.setIsLike(isLike > 0);
		User author = userService.getById(blog.getUserId());
		UserVO authorVO = new UserVO();
		BeanUtils.copyProperties(author, authorVO);
		LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
		followLambdaQueryWrapper.eq(Follow::getFollowUserId, authorVO.getId()).eq(Follow::getUserId, userId);
		long count = followService.count(followLambdaQueryWrapper);
		authorVO.setIsFollow(count > 0);
		blogVO.setAuthor(authorVO);
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
}




