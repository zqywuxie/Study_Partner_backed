package com.example.studypartner.domain.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 *
 * @author wuxie
 * @TableName user
 */
@ApiModel(description = "用户表")
@Data
public class UserUpdateRequest implements Serializable {

	@ApiModelProperty("")
	private static final long serialVersionUID = 1760524879185272823L;


	@ApiModelProperty("用户id")
	private Long id;

	/**
	 * 用户昵称
	 */
	@ApiModelProperty("用户昵称")

	private String username;


	/**
	 * 性别
	 */
	@ApiModelProperty("性别")
	private Integer gender;


	/**
	 * 邮箱
	 */
	@ApiModelProperty("邮箱")
	private String email;


	/**
	 * 电话
	 */
	@ApiModelProperty("电话")
	private String phone;


	/**
	 * 用户城市
	 */
	@ApiModelProperty("用户城市")
	private String city;

	/**
	 * 用户个人简历
	 */

	@ApiModelProperty("用户个人简历")
	private String profile;

	/**
	 * 用户省
	 */

	@ApiModelProperty("用户省")
	private String province;

	private String tags;

}