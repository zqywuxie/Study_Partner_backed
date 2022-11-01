package com.example.usercenterback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenterback.common.CommonResult;
import com.example.usercenterback.common.ErrorCode;
import com.example.usercenterback.domain.User;
import com.example.usercenterback.exception.ResultException;
import com.example.usercenterback.mapper.UserMapper;
import com.example.usercenterback.service.UserService;
import com.example.usercenterback.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.usercenterback.constant.UserConstant.*;

/**
 * @author 思无邪
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createTime 2022-10-10 16:54:41
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    @Autowired
    UserMapper userMapper;

    // 加密盐值
    public static final String SALT = "zqy";


    @Override
    public CommonResult<Long> Register(String usercount, String password, String checkPassword) {
        // 信息不能为空
        if (StringUtils.isAllBlank(usercount, password, checkPassword)) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);

        }
        // 账号大于4位
        if (usercount.length() < 4) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);

        }
        // 密码大于6位
        if (password.length() < 6) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);


        }

        // 密码与校验密码一致
        if (!password.equals(checkPassword)) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);

        }
        // 账号不能有特殊符号
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(usercount);
        if (matcher.find()) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);

        }
        // 查看账号是否有重复
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", usercount);
        Long count = userMapper.selectCount(userQueryWrapper);
        if (count > 0) {
            throw new ResultException(ErrorCode.REPEAT_ERROR);
        }
        // 密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        // 插入数据
        User user = new User();
        user.setUserAccount(usercount);
        user.setUserPassword(encryptPassword);
        user.setAvatarUrl(Default_Avatar);
        user.setUsername(Default_Name);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return ResultUtils.failed(ErrorCode.INSERT_ERROR);
        }
        Long id = user.getId();
        return ResultUtils.success(id);
    }

    @Override
    public CommonResult<User> Login(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.数据校验
        // 信息不能为空
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);

        }
        // 账号大于4位
        if (userAccount.length() < 4) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);

        }
        // 密码大于6位
        if (userPassword.length() < 6) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);

        }
        // 账号不能有特殊符号
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);

        }
        // 2.查看账号是否有重复
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        User selectOne = userMapper.selectOne(userQueryWrapper);
        if (selectOne == null) {
            throw new ResultException(ErrorCode.REPEAT_ERROR);
        }
        // 2.检查用户是否存在
        // 密码加密
        final String SALT = "zqy";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("login failed,please check your account and userPassword");
            throw new ResultException(ErrorCode.NULL_ERROR);
        }
        User cleanUser = cleanUser(user).getData();
        request.getSession().setAttribute(User_Login_Status, cleanUser);
        return ResultUtils.success(cleanUser);
    }


    /**
     * 用户脱敏
     *
     * @param user
     * @return
     */
    @Override
    public CommonResult<User> cleanUser(User user) {
        if (user == null) {
            throw new ResultException(ErrorCode.REPEAT_ERROR);
        }
        // 3.用户存在进行脱敏
        user.setId(user.getId());
        user.setUsername(user.getUsername());
        user.setUserAccount(user.getUserAccount());
        user.setAvatarUrl(user.getAvatarUrl());
        user.setGender(user.getGender());
        user.setEmail(user.getEmail());
        user.setUserStatus(user.getUserStatus());
        user.setPhone(user.getPhone());
        user.setCreateTime(user.getCreateTime());
        user.setCity(user.getCity());
        user.setProvince(user.getProvince());
        user.setProfile(user.getProfile());
        // 将数据通过session进行传入
        return ResultUtils.success(user);
    }
}





