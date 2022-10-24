package com.example.usercenterback.domain;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 * @TableName user
 */
@ApiModel(description = "用户表")
@TableName(value ="user")
@Data
public class User implements Serializable {

    private static final long serialVersionUID = 1760524879185272823L;
    /**
     * 用户id
     */
    @ApiModelProperty("用户id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户昵称
     */
    @ApiModelProperty("用户昵称")

    private String username;

    /**
     * 账号
     */
    @ApiModelProperty("账号")

    private String userAccount;

    /**
     * 用户头像
     */
    @ApiModelProperty("用户头像")

    private String avatarUrl;

    /**
     * 性别
     */
    @ApiModelProperty("性别")
    private Integer gender;

    /**
     * 密码
     */
    @ApiModelProperty("密码")
    private String userPassword;

    /**
     * 邮箱
     */
    @ApiModelProperty("邮箱")
    private String email;

    /**
     * 状态 0-正常
     */
    @ApiModelProperty("状态 0-正常")
    private Integer userStatus;

    /**
     * 电话
     */
    @ApiModelProperty("电话")
    private String phone;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private Date updateTime;

    /**
     * 是否删除
     * 设置逻辑删除
     */
    @ApiModelProperty("是否删除 设置逻辑删除")
    @TableLogic(value = "0",delval = "1")
    private Integer isDelete;

    /**
     * 用户权限
     * 0 普通用户
     * 1 管理员
     */
    @ApiModelProperty("用户权限 0 普通用户 1 管理员")
    private  Integer userRole;
    
}