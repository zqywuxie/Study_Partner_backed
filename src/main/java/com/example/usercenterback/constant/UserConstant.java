package com.example.usercenterback.constant;

/**
 * 2022/10/11
 * 用户内容相关的常量
 * @version 1.0
 * @Author:zqy
 */
public interface UserConstant {

    String User_Login_Status = "userLoginStatus";
    /**
     * 管理员权限
     */
    Integer ADMIN_ROLE=1;
    /**
     * 普通用户权限
     */
    Integer DEFAULT_ROLE=0;

    /**
     * 用户默认头像
     */
    String Default_Avatar ="https://xingqiu-tuchuang-1256524210.cos.ap-shanghai.myqcloud.com/5339/QQ.jpg";

    String Default_Name="Default";
}
