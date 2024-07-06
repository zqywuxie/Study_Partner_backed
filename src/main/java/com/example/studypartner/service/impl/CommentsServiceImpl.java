package com.example.studypartner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.constant.UserConstant;
import com.example.studypartner.domain.entity.*;
import com.example.studypartner.domain.enums.MessageTypeEnum;
import com.example.studypartner.domain.request.CommentsAddRequest;
import com.example.studypartner.domain.vo.BlogVO;
import com.example.studypartner.domain.vo.CommentsVO;
import com.example.studypartner.domain.vo.UserVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.mapper.CommentsMapper;
import com.example.studypartner.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.studypartner.constant.RedisConstants.*;

/**
 * @author wuxie
 * @description 针对表【comments】的数据库操作Service实现
 * @createDate 2023-11-21 14:41:58
 */
@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments>
		implements CommentsService {
	@Resource
	private UserService userService;

	@Resource
	private BlogService blogService;

	@Resource
	private CommentLikeService commentLikeService;


	@Resource
	private MessageService messageService;

	@Resource
	private RedisTemplate redisTemplate;

	@Override
	@Transactional
	public void addComment(CommentsAddRequest addCommentRequest, Long userId) {
		Comments comments = new Comments();
		comments.setUserId(userId);
		comments.setBlogId(addCommentRequest.getBlogId());
		comments.setContent(addCommentRequest.getContent());
		comments.setLikedNum(0);
		comments.setStatus(0);
		this.save(comments);

		Blog blog = blogService.getById(addCommentRequest.getBlogId());
		LambdaUpdateWrapper<Blog> blogLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
		blogLambdaUpdateWrapper.eq(Blog::getId, addCommentRequest.getBlogId())
				.set(Blog::getCommentsNum, blog.getCommentsNum() + 1);
		blogService.update(blogLambdaUpdateWrapper);

		// todo 添加消息
		Message message = new Message();
		message.setType(MessageTypeEnum.COMMENT_ADD.getValue());
		message.setFromId(userId);
		message.setToId(blog.getUserId());
		//添加评论内容
		message.setData(String.valueOf(comments.getId()));
		messageService.save(message);
		//todo
		String likeNumKey = MESSAGE_COMMENT_NUM_KEY + blog.getUserId();
		Boolean hasKey = redisTemplate.hasKey(likeNumKey);
		if (Boolean.TRUE.equals(hasKey)) {
			redisTemplate.opsForValue().increment(likeNumKey);
		} else {
			redisTemplate.opsForValue().set(likeNumKey, 1);
		}

	}

	@Override
	public List<CommentsVO> listComments(long blogId, long userId) {
		LambdaQueryWrapper<Comments> CommentsLambdaQueryWrapper = new LambdaQueryWrapper<>();
		CommentsLambdaQueryWrapper.eq(Comments::getBlogId, blogId);
		List<Comments> list = this.list(CommentsLambdaQueryWrapper);
		return list.stream().map((comment) -> {
			CommentsVO commentsVO = new CommentsVO();
			BeanUtils.copyProperties(comment, commentsVO);

			User user = userService.getById(comment.getUserId());
			if (user == null) {
				throw new ResultException(ErrorCode.PARAMS_ERROR, "评论没有用户");
			}
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(user, userVO);
			commentsVO.setCommentUser(userVO);

			LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
			commentLikeLambdaQueryWrapper.eq(CommentLike::getCommentId, comment.getId()).eq(CommentLike::getUserId, userId);
			long count = commentLikeService.count(commentLikeLambdaQueryWrapper);
			commentsVO.setIsLiked(count > 0);
			return commentsVO;
		}).collect(Collectors.toList());
	}

	@Override
	public CommentsVO getComment(long commentId, Long userId) {
		Comments comments = this.getById(commentId);
		CommentsVO commentsVO = new CommentsVO();
		BeanUtils.copyProperties(comments, commentsVO);
		LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
		commentLikeLambdaQueryWrapper.eq(CommentLike::getUserId, userId).eq(CommentLike::getCommentId, commentId);
		long count = commentLikeService.count(commentLikeLambdaQueryWrapper);
		commentsVO.setIsLiked(count > 0);
		return commentsVO;
	}

	@Override
	@Transactional
	public void likeComment(long commentId, Long userId) {
		LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
		commentLikeLambdaQueryWrapper.eq(CommentLike::getCommentId, commentId).eq(CommentLike::getUserId, userId);
		long count = commentLikeService.count(commentLikeLambdaQueryWrapper);
		String likeCountKey = LIKE_COUNT_KEY + userId;

		if (count == 0) {
			CommentLike commentLike = new CommentLike();
			commentLike.setCommentId(commentId);
			commentLike.setUserId(userId);
			// 添加 todo 点赞信息存数据库是否太浪费资源了？
			commentLikeService.save(commentLike);
			Comments comments = this.getById(commentId);


			// 评论点赞数增加
			this.update().eq("id", commentId)
					.set("liked_num", comments.getLikedNum() + 1).update();

			String likeNumKey = MESSAGE_LIKE_NUM_KEY + comments.getId();

			Boolean hasKey = redisTemplate.hasKey(likeNumKey);
			Boolean countKey = redisTemplate.hasKey(likeCountKey);
			if (Boolean.TRUE.equals(countKey)) {
				redisTemplate.opsForValue().increment(likeCountKey);

			} else {
				redisTemplate.opsForValue().set(likeCountKey, 1);

			}
			if (Boolean.TRUE.equals(hasKey)) {
				redisTemplate.opsForValue().increment(likeNumKey);
			} else {
				redisTemplate.opsForValue().set(likeNumKey, 1);
			}
			Message message = new Message();
			message.setType(MessageTypeEnum.BLOG_COMMENT_LIKE.getValue());
			message.setFromId(userId);
			message.setToId(comments.getUserId());
			message.setData(String.valueOf(comments.getId()));
			messageService.save(message);
		} else {
			commentLikeService.remove(commentLikeLambdaQueryWrapper);
			Comments comments = this.getById(commentId);
			String likeNumKey = MESSAGE_LIKE_NUM_KEY + comments.getId();

			Boolean hasKey = redisTemplate.hasKey(likeNumKey);
			if (Boolean.TRUE.equals(hasKey)) {
				redisTemplate.opsForValue().decrement(likeNumKey);
				redisTemplate.opsForValue().decrement(likeCountKey);
			}
			// 评论点赞数减少
			this.update().eq("id", commentId)
					.set("liked_num", comments.getLikedNum() - 1).update();
		}
	}

	@Override
	public void deleteComment(Long id, Long userId) {
		User user = userService.getById(userId);
		Comments comments = this.getById(id);
		if (comments == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		if (!comments.getUserId().equals(user.getId())) {
			throw new ResultException(ErrorCode.NO_AUTH);
		}
		this.removeById(id);
		Integer commentsNum = blogService.getById(comments.getBlogId()).getCommentsNum();
		LambdaUpdateWrapper<Blog> blogLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
		blogLambdaUpdateWrapper.eq(Blog::getId, comments.getBlogId()).set(Blog::getCommentsNum, commentsNum - 1);
		blogService.update(blogLambdaUpdateWrapper);
		return;
	}

	@Override
	public List<CommentsVO> listMyComments(Long id) {
		LambdaQueryWrapper<Comments> CommentsLambdaQueryWrapper = new LambdaQueryWrapper<>();
		CommentsLambdaQueryWrapper.eq(Comments::getUserId, id);
		List<Comments> CommentsList = this.list(CommentsLambdaQueryWrapper);

		return CommentsList.stream().map((comments) -> {
			CommentsVO CommentsVO = new CommentsVO();
			BeanUtils.copyProperties(comments, CommentsVO);

			User user = userService.getById(comments.getUserId());
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(user, userVO);
			CommentsVO.setCommentUser(userVO);

			Long blogId = CommentsVO.getBlogId();
			Blog blog = blogService.getById(blogId);
			BlogVO blogVO = new BlogVO();
			BeanUtils.copyProperties(blog, blogVO);
			String images = blogVO.getImages();
			if (images == null) {
				blogVO.setCoverImage(null);
			} else {
				String[] imgStr = images.split(",");
				blogVO.setCoverImage(imgStr[0]);
			}
			Long authorId = blogVO.getUserId();
			User author = userService.getById(authorId);
			UserVO authorVO = new UserVO();
			BeanUtils.copyProperties(author, authorVO);
			blogVO.setAuthor(authorVO);

			CommentsVO.setBlogVO(blogVO);

			LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
			commentLikeLambdaQueryWrapper.eq(CommentLike::getUserId, id).eq(CommentLike::getCommentId, comments.getId());
			long count = commentLikeService.count(commentLikeLambdaQueryWrapper);
			CommentsVO.setIsLiked(count > 0);
			return CommentsVO;
		}).collect(Collectors.toList());
	}


	//todo
	@Override
	public List<CommentsVO> listMyBlogComments(Long id) {
		return null;
	}
}




