package com.example.studypartner.domain.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 2022/10/11
 * 将注册数据封装为一个类
 * @version 1.0
 * @Author:zqy
 */
@Data
public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = -2227307106629029499L;
    @ApiModelProperty("用户账号")
    private String userAccount;
    @ApiModelProperty("用户密码")
    private String userPassword;
    @ApiModelProperty("核实密码")
    private String checkPassword;

    @ApiModelProperty("头像地址")
    private String avatarUrl;

    @ApiModelProperty("用户名")
    private String userName;
}
