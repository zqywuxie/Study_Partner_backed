package com.example.studypartner.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.example.studypartner.domain.entity.Blog;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 博客vo
 *
 * @author wuxie
 * @date 2023/06/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BlogVO extends Blog implements Serializable {
	/**
	 * 串行版本uid
	 */
	private static final long serialVersionUID = -1461567317259590205L;

	private Long id;
	/**
	 * 标题
	 */
	private String title;


	/**
	 * 文章
	 */
	private String content;

	/**
	 * 点赞数量
	 */
	private Integer likedNum;

	/**
	 * 评论数量
	 */
	private Integer commentsNum;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 登录用户是否点赞
	 */
	@ApiModelProperty(value = "是否点赞")
	private Boolean isLike;
	/**
	 * 封面图片
	 */
	@ApiModelProperty(value = "封面图片")
	private String coverImage;
	/**
	 * 作者
	 */
	@ApiModelProperty(value = "作者")
	private UserVO author;

}
