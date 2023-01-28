package com.example.studypartner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 思无邪
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2022-10-10 16:54:41
*/
public interface UserService extends IService<User> {
    /**
     *
     * @param usercount 账号
     * @param password 账号密码
     * @param checkPassword 校验密码
     * @return 返回用户id
     */
     CommonResult<Long> Register(String usercount, String password, String checkPassword);

    /**
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
     CommonResult<User> Login(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 用户信息脱敏
     * @param user
     * @return
     */
     CommonResult<User> cleanUser(User user);


    /**
     * 根据标签名进行查找用户
     *
     * @param tagNameList
     * @return
     */
    List<CommonResult<User>> searchUserByTags(List<String> tagNameList);

    List<CommonResult<User>> memorySearch(List<String> tagNameList);
}
