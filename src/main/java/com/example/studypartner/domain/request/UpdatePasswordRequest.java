package com.example.studypartner.domain.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 更新密码请求
 *
 * @author wuxie
 * @date 2023/06/22
 */
@Data
@ApiModel(value = "密码更新请求")
public class UpdatePasswordRequest {
	/**
	 * 邮箱
	 */
	@ApiModelProperty(value = "邮箱")
	private String email;

	/**
	 * 密码
	 */
	@ApiModelProperty(value = "密码")
	private String password;
}
