package com.example.studypartner.domain.vo;

import com.example.studypartner.domain.entity.Comments;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName comments
 */
@Data
public class CommentsVO extends Comments implements Serializable {


	/**
	 * 评论者
	 */
	private UserVO commentUser;

	/**
	 * 是否点赞
	 */
	private Boolean isLiked;


	/**
	 * 相关博文
	 */
	private BlogVO blogVO;

	private static final long serialVersionUID = 1L;
}