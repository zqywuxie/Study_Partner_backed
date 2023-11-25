package com.example.studypartner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.studypartner.domain.entity.Comments;
import com.example.studypartner.domain.request.CommentsAddRequest;
import com.example.studypartner.domain.vo.CommentsVO;

import java.util.List;

/**
 * @author wuxie
 * @description 针对表【comments】的数据库操作Service
 * @createDate 2023-11-21 14:41:58
 */
public interface CommentsService extends IService<Comments> {
	void addComment(CommentsAddRequest addCommentRequest, Long userId);

	List<CommentsVO> listComments(long blogId, long userId);

	/**
	 * 获得某个评论
	 *
	 * @param commentId
	 * @param userId
	 * @return
	 */
	CommentsVO getComment(long commentId, Long userId);


	/**
	 * 点赞
	 *
	 * @param commentId
	 * @param userId
	 */
	void likeComment(long commentId, Long userId);

	void deleteComment(Long id, Long userId);

	/**
	 * 获得我的评论
	 *
	 * @param id
	 * @return
	 */
	List<CommentsVO> listMyComments(Long id);


	/**
	 * 获得我的博文评论
	 * @param id
	 * @return
	 */
	List<CommentsVO> listMyBlogComments(Long id);


}
