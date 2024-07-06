package com.example.studypartner.domain.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 2022/10/11
 * 将注册数据封装为一个类
 *
 * @version 1.0
 * @Author:zqy
 */
@Data
public class RegisterRequest implements Serializable {
	private static final long serialVersionUID = -2227307106629029499L;
	@ApiModelProperty("用户账号")
	private String useraccount;


	@ApiModelProperty("用户名")
	private String userName;

	@ApiModelProperty("用户密码")
	private String password;

	@ApiModelProperty("核实密码")
	private String checkPassword;

	@ApiModelProperty("邮箱")

	private String email;

	@ApiModelProperty("头像地址")
	private String avatarUrl;

	@ApiModelProperty("验证码")
	private String captcha;

}
