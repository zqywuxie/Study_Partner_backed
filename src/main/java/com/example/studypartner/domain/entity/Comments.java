package com.example.studypartner.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName comments
 */
@TableName(value ="comments")
@Data
public class Comments implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long blogId;

    /**
     * 
     */
    private Long userId;

    /**
     * 
     */
    private String content;

    /**
     * 
     */
    private Integer likedNum;



    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 状态，0：正常，1：被举报，2：禁止查看
     */
    private Integer status;

    /**
     * 
     */
    private Integer parentCommentId;

    /**
     * 
     */
    private Integer childCommentId;

	@ApiModelProperty("是否删除 设置逻辑删除")
	@TableLogic(value = "0", delval = "1")
	@TableField("deleted")
	private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}