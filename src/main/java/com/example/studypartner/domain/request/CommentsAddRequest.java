package com.example.studypartner.domain.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName comments
 */
@Data
public class CommentsAddRequest implements Serializable {


	/**
	 *
	 */
	private Long blogId;
	/**
	 *
	 */
	private String content;
	/**
	 *
	 */
	private Integer parentCommentId;

	@TableField(exist = false)
	private static final long serialVersionUID = 1L;
}