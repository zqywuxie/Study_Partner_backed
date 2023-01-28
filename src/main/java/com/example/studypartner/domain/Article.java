package com.example.studypartner.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户表
 * @TableName article
 */
@TableName(value ="article")
@Data
public class Article implements Serializable {
    /**
     * 文章id
     */
    @ApiModelProperty("文章id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 账号
     */
    @ApiModelProperty("文章账号")
    private String userAccount;

    /**
     * 用户头像
     */
    @ApiModelProperty("文章发布者头像链接")
    private String avatarUrl;

    /**
     * 正文
     */
    @ApiModelProperty("文章内容")

    private String content;

    /**
     * 标题
     */
    @ApiModelProperty("文章标题")

    private String title;

    /**
     * 描述
     */
    @ApiModelProperty("文章描述")

    private String description;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}