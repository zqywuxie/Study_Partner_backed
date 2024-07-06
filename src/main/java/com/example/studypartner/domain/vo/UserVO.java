package com.example.studypartner.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户包装类（脱敏）
 */
@Data
public class UserVO implements Serializable {

	/**
	 * id
	 */
	private Long id;

	/**
	 * 用户昵称
	 */
	private String username;

	/**
	 * 账号
	 */
	private String useraccount;

	/**
	 * 用户头像
	 */
	private String avatarUrl;

	private String profile;

	/**
	 * 性别
	 */
	private Integer gender;

	/**
	 * 电话
	 */
	private String phone;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 标签列表 json
	 */
	private String tags;

	/**
	 * 状态 0 - 正常
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 *
	 */
	private Date updateTime;

//	/**
//	 * 用户角色 0 - 普通用户 1 - 管理员
//	 */
//	private Integer userRole;


	/**
	 * 是否关注
	 */
	@ApiModelProperty(value = "是否关注")
	private Boolean isFollow;

	/**
	 * 是否关注
	 */
	@ApiModelProperty(value = "是否关注")
	private Boolean isFriend;

	private static final long serialVersionUID = -2643066616918515217L;
}