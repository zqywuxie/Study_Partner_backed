package com.example.usercenterback.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 2022/10/11
 *将登录数据封装为一个类
 * @version 1.0
 * @Author:zqy
 */
@Data
public class LoginInfo implements Serializable {
    private static final long serialVersionUID = 1140279174545179366L;
    private String userAccount;
    private String userPassword;
}
