package com.example.usercenterback.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.usercenterback.common.CommonResult;
import com.example.usercenterback.common.ErrorCode;
import com.example.usercenterback.domain.Article;
import com.example.usercenterback.domain.User;
import com.example.usercenterback.domain.request.LoginInfo;
import com.example.usercenterback.domain.request.RegisterInfo;
import com.example.usercenterback.exception.ResultException;
import com.example.usercenterback.service.ArticleService;
import com.example.usercenterback.service.UserService;
import com.example.usercenterback.utils.ResultUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.usercenterback.constant.UserConstant.ADMIN_ROLE;
import static com.example.usercenterback.constant.UserConstant.User_Login_Status;

/**
 * 2022/10/11
 *
 * @version 1.0
 * @Author:zqy
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    ArticleService articleService;

    /**
     * 登录接口
     *
     * @param loginInfo
     * @param request
     * @return
     */
    @PostMapping("/login")
    public CommonResult<User> Login(@RequestBody LoginInfo loginInfo, HttpServletRequest request) {
        if (loginInfo == null) {
            throw new ResultException(ErrorCode.NULL_ERROR);
        }
        String userAccount = loginInfo.getUserAccount();
        String userPassword = loginInfo.getUserPassword();
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            throw new ResultException(ErrorCode.NULL_ERROR);
        }
        User user = userService.Login(userAccount, userPassword, request).getData();
        return ResultUtils.success(user);
    }

    /**
     * 注册接口
     *
     * @param registerInfo
     * @return
     */

    @PostMapping("/register")
    public CommonResult<Long> Register(@RequestBody RegisterInfo registerInfo) {
        if (registerInfo == null) {
            throw new ResultException(ErrorCode.NULL_ERROR);

        }
        String userCount = registerInfo.getUserAccount();
        String passWord = registerInfo.getUserPassword();
        String checkPassword = registerInfo.getCheckPassword();
        if (StringUtils.isAllBlank(userCount, passWord, checkPassword)) {
            throw new ResultException(ErrorCode.NULL_ERROR);

        }
        long register = userService.Register(userCount, passWord, checkPassword).getData();
        return ResultUtils.success(register);
    }

    /**
     * 删除接口
     *
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public CommonResult<Boolean> Delete(@RequestBody User user,HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new ResultException(ErrorCode.NOT_ADMIN);
        }

        if (user==null) {
            throw new ResultException(ErrorCode.NULL_ERROR);

        }
        boolean delete = userService.removeById(user);
        return ResultUtils.success(delete);
    }

    /**
     * 根据用户名查询
     * 管理接口
     *
     * @param username
     * @param request
     * @return
     */
    @GetMapping("/search")
    public CommonResult<List<User>> users(String username, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new ResultException(ErrorCode.NOT_ADMIN);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(username)) {
            userQueryWrapper.like("username", username);
        }
        List<User> list = userService.list(userQueryWrapper);
        List<User> userList = list.stream().map(user -> userService.cleanUser(user).getData()).collect(Collectors.toList());
        return ResultUtils.success(userList);
    }

    /**
     * 校验用户权限
     *
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(User_Login_Status);
        User user = (User) attribute;
        if (user == null || user.getUserRole() != ADMIN_ROLE) {
            return false;
        }
        return true;
    }

    /**
     * 根据session获得当前用户数据
     * @param request
     * @return
     */
    @GetMapping("/current")
    public CommonResult<User> currentUser(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(User_Login_Status);
        User currentUser = (User) attribute;
        if (currentUser == null) {
            // throw new ResultException(ErrorCode.NULL_ERROR);
            return null;
        }
        Long id = currentUser.getId();
        User user = userService.getById(id);
        return ResultUtils.success(user);
    }

    /**
     * 退出登录 删除用户登录态
     * @param request
     * @return
     */
    @PostMapping("/outLogin")
    public CommonResult<Integer> outLogin(HttpServletRequest request) {
        if (request == null) {
            throw new ResultException(ErrorCode.NULL_ERROR);
        }
        request.getSession().removeAttribute(User_Login_Status);
        return ResultUtils.success(1);
    }

    /**
     * 更新数据
     * @param user
     * @return
     */
    @PostMapping("/change")
    public CommonResult<Boolean> update(@RequestBody User user,HttpServletRequest request)
    {
        if (!isAdmin(request))
        {
            throw new ResultException(ErrorCode.NOT_ADMIN);
        }
        if (user==null)
        {
            throw new ResultException(ErrorCode.NULL_ERROR);
        }

        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
    }

    /**
     *  添加数据
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/insert")
    public CommonResult<Boolean>insert(@RequestBody User user,HttpServletRequest request)
    {
        if (!isAdmin(request))
        {
            throw new ResultException(ErrorCode.NOT_ADMIN);
        }
        if (user==null)
        {
            throw new ResultException(ErrorCode.NULL_ERROR);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("username",user.getUsername());
        userQueryWrapper.eq("userAccount",user.getUserAccount());
        User one = userService.getOne(userQueryWrapper);
        if (one!=null)
        {
        throw new ResultException(ErrorCode.REPEAT_ERROR);
        }
        boolean save = userService.save(user);
        return ResultUtils.success(save);
    }

    //===========文章数据获取
    @GetMapping("/article")
    public CommonResult<List<Article>> article(String userAccount)
    {
        QueryWrapper<Article> articleQueryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(userAccount)) {
            articleQueryWrapper.like("userAccount", userAccount);
        }
        List<Article> list = articleService.list(articleQueryWrapper);
        return ResultUtils.success(list);
    }
}
