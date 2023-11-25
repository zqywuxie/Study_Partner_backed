package com.example.studypartner.domain.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 2022/10/11
 *将登录数据封装为一个类
 * @version 1.0
 * @Author:zqy
 */
@Data
public class LoginByEmailRequest implements Serializable {
    private static final long serialVersionUID = 1140279174545179366L;
    @ApiModelProperty("用户邮箱")
    private String email;
    @ApiModelProperty("验证码")
    private String captcha;
}
