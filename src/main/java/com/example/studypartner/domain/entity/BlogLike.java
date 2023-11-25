package com.example.studypartner.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName bloglike
 */
@TableName(value = "blog_like")
@Data
public class BlogLike implements Serializable {
	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 博文id
	 */
	private Long blogId;

	/**
	 * 用户id
	 */
	private Long userId;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;

	/**
	 * 逻辑删除
	 */
	private Integer isDelete;

	@TableField(exist = false)
	private static final long serialVersionUID = 1L;
}