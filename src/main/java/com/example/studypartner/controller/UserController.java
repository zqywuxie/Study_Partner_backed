package com.example.studypartner.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.User;
import com.example.studypartner.domain.request.LoginInfo;
import com.example.studypartner.domain.request.RegisterInfo;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.studypartner.constant.UserConstant.User_Login_Status;

/**
 * 2022/10/11
 *
 * @version 1.0
 * @Author:zqy
 */
@Api(value = "UserController", tags = "用户接口")

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173/"})
public class UserController {
    @Autowired
    UserService userService;


    /**
     * 登录接口
     *
     * @param loginInfo
     * @param request
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "LoginInfo", name = "loginInfo", value = "", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "request", value = "", required = true)
    })
    @ApiOperation(value = "登录接口", notes = "登录接口", httpMethod = "POST")
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

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "RegisterInfo", name = "registerInfo", value = "", required = true)
    })
    @ApiOperation(value = "注册接口", notes = "注册接口", httpMethod = "POST")
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
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "User", name = "user", value = "", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "request", value = "", required = true)
    })
    @ApiOperation(value = "删除接口", notes = "删除接口", httpMethod = "POST")
    @PostMapping("/delete")
    public CommonResult<Boolean> Delete(@RequestBody User user, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new ResultException(ErrorCode.NOT_ADMIN);
        }

        if (user == null) {
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
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "string", name = "username", value = "", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "request", value = "", required = true)
    })
    @ApiOperation(value = "根据用户名查询 管理接口", notes = "根据用户名查询 管理接口", httpMethod = "GET")
    @GetMapping("/search")
    public CommonResult<List<User>> users(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
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
     * 根据session获得当前用户数据
     *
     * @param request
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "request", value = "", required = true)
    })
    @ApiOperation(value = "根据session获得当前用户数据", notes = "根据session获得当前用户数据", httpMethod = "GET")
    @GetMapping("/current")
    public CommonResult<User> currentUser(HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
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
     *
     * @param request
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "request", value = "", required = true)
    })
    @ApiOperation(value = "退出登录 删除用户登录态", notes = "退出登录 删除用户登录态", httpMethod = "POST")
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
     *
     * @param user
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "User", name = "user", value = "", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "request", value = "", required = true)
    })
    @ApiOperation(value = "更新数据", notes = "更新数据", httpMethod = "POST")
    @PostMapping("/change")
    public CommonResult<Integer> update(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Integer result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 添加数据
     *
     * @param user
     * @param request
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "User", name = "user", value = "", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "request", value = "", required = true)
    })
    @ApiOperation(value = "添加数据", notes = "添加数据", httpMethod = "POST")
    @PostMapping("/insert")
    public CommonResult<Boolean> insert(@RequestBody User user, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new ResultException(ErrorCode.NOT_ADMIN);
        }
        if (user == null) {
            throw new ResultException(ErrorCode.NULL_ERROR);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("username", user.getUsername());
        userQueryWrapper.eq("userAccount", user.getUserAccount());
        User one = userService.getOne(userQueryWrapper);
        if (one != null) {
            throw new ResultException(ErrorCode.REPEAT_ERROR);
        }
        boolean save = userService.save(user);
        return ResultUtils.success(save);
    }

    @GetMapping("/search/tags")
    public CommonResult<List<CommonResult<User>>> searchUsersByTags(@RequestParam(required = false) List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            throw new ResultException(ErrorCode.NULL_ERROR);
        }
        List<CommonResult<User>> userList = userService.searchUserByTags(tags);
        return ResultUtils.success(userList);
    }

}
