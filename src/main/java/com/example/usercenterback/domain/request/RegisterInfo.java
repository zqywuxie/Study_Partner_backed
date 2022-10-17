package com.example.usercenterback.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 2022/10/11
 * 将注册数据封装为一个类
 * @version 1.0
 * @Author:zqy
 */
@Data
public class RegisterInfo implements Serializable {
    private static final long serialVersionUID = -2227307106629029499L;
    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
