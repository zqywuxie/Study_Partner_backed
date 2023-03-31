package com.example.studypartner.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.User;
import com.example.studypartner.domain.request.LoginRequest;
import com.example.studypartner.domain.request.RegisterRequest;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.studypartner.constant.UserConstant.User_Login_Status;

/**
 * @author wuxie
 * 用户数据接口
 */


@Api(value = "/user", tags = {"用户数据接口"})
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173/"})
@Slf4j
public class UserController {
    @Autowired
    UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 登录接口
     *
     * @param loginRequest
     * @param request
     * @return
     */

    @PostMapping("/login")
    public CommonResult<User> Login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        if (loginRequest == null) {
            throw new ResultException(ErrorCode.NULL_ERROR);
        }
        String userAccount = loginRequest.getUserAccount();
        String userPassword = loginRequest.getUserPassword();
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            throw new ResultException(ErrorCode.NULL_ERROR);
        }
        User user = userService.Login(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 注册接口
     *
     * @param registerRequest
     * @return
     */


    @PostMapping("/register")
    public CommonResult<Long> Register(@RequestBody RegisterRequest registerRequest) {
        if (registerRequest == null) {
            throw new ResultException(ErrorCode.NULL_ERROR);

        }
        String userCount = registerRequest.getUserAccount();
        String passWord = registerRequest.getUserPassword();
        String checkPassword = registerRequest.getCheckPassword();
        String avatarUrl = registerRequest.getAvatarUrl();
        String userName = registerRequest.getUserName();
        if (StringUtils.isAllBlank(userCount, passWord, checkPassword, avatarUrl, userName)) {
            throw new ResultException(ErrorCode.NULL_ERROR);

        }
        long register = userService.Register(userCount, passWord, checkPassword, avatarUrl, userName);
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
        List<User> userList = list.stream().map(user -> userService.cleanUser(user)).collect(Collectors.toList());
        return ResultUtils.success(userList);
    }

    /**
     * 主页推荐用户
     *
     * @param pageSize
     * @param pageNum
     * @param request
     * @return
     */

    @GetMapping("/recommend")
    /**
     todo 后期优化 将业务逻辑写到service里面
     *
     */
    public CommonResult<Page<User>> recommend(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new ResultException(ErrorCode.NO_LOGIN);
        }
        String rediskey = String.format("wuxie:user:recommend:%s", loginUser.getId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) valueOperations.get(rediskey);
        if (userPage != null) {
            ResultUtils.success(userPage);
        }
//写缓存,并且设置缓存时间
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        Page<User> page = userService.page(new Page<>(pageNum, pageSize), userQueryWrapper);
        try {
            valueOperations.set(rediskey, page, 30000, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("redis set key-value error");
        }
        return ResultUtils.success(page);
    }


    /**
     * 根据session获得当前用户数据
     *
     * @param request
     * @return
     */

    @GetMapping("/current")
    public CommonResult<User> currentUser(HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        if (currentUser == null) {
            throw new ResultException(ErrorCode.NO_LOGIN);
//            return null;
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

    @PostMapping("/change")
    public CommonResult<Integer> update(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        System.out.println(user);
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

    /**
     * 根据标签查询用户
     *
     * @param tags
     * @return
     */

    @GetMapping("/search/tags")
    public CommonResult<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            throw new ResultException(ErrorCode.NULL_ERROR);
        }
        List<User> userList = userService.searchUserByTags(tags);
        return ResultUtils.success(userList);
    }

    @GetMapping("/match")
    public CommonResult<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser==null){
            throw new ResultException(ErrorCode.NO_LOGIN);
        }
        return ResultUtils.success(userService.matchUsers(num, loginUser));

    }

}
