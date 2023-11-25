package com.example.studypartner.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.constant.RedisConstants;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.request.*;
import com.example.studypartner.domain.vo.UserVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.studypartner.constant.EmailConstant.CAPTCHA_CACHE_KEY;
import static com.example.studypartner.constant.RedisConstants.USER_RECOMMEND_KEY;
import static com.example.studypartner.constant.UserConstant.USER_LOGIN_STATUS;

/**
 * @author wuxie
 * 用户数据接口
 */


@Api(value = "/user", tags = {"用户数据接口"})
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"https://www.zqywuku.top/"}, allowCredentials = "true")
//@CrossOrigin(origins = {"http://localhost:5173"}, allowCredentials = "true")
@Slf4j
public class UserController {
	@Autowired
	UserService userService;

	@Resource
	private RedisTemplate redisTemplate;


	//region 登录注册接口

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
		User user = userService.login(userAccount, userPassword, request);
		return ResultUtils.success(user);
	}

	/**
	 * 根据邮箱登录
	 *
	 * @param loginRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/email/login")
	public CommonResult<User> LoginByEmail(@RequestBody LoginByEmailRequest loginByEmailRequest, HttpServletRequest httpServletRequest) {
		if (loginByEmailRequest == null) {
			return ResultUtils.failed(ErrorCode.NULL_ERROR);
		}
		String email = loginByEmailRequest.getEmail();
		String captcha = loginByEmailRequest.getCaptcha();
		String checkCaptcha = (String) redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + email);
		System.out.println(captcha + ":" + checkCaptcha);
		if (!captcha.equals(checkCaptcha)) {
			return ResultUtils.failed(ErrorCode.PARAMS_ERROR, "验证码不正确，请重新输入");
		}
		User user = userService.loginByEmail(email, httpServletRequest);
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
		request.getSession().removeAttribute(USER_LOGIN_STATUS);
		return ResultUtils.success(1);
	}

	/**
	 * 注册接口
	 *
	 * @param registerRequest
	 * @return
	 */


	private void validateRegistrationRequest(RegisterRequest registerRequest) {
		if (registerRequest == null || StringUtils.isAnyBlank(
				registerRequest.getUserAccount(),
				registerRequest.getUserPassword(),
				registerRequest.getCheckPassword(),
//				registerRequest.getAvatarUrl(),
//				registerRequest.getUserName(),
				registerRequest.getEmail(),
				registerRequest.getCaptcha())) {
			throw new ResultException(ErrorCode.NULL_ERROR);
		}
		if (!registerRequest.getCheckPassword().equals(registerRequest.getUserPassword())) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
		}
	}

	@PostMapping("/register")
	public CommonResult<String> register(@RequestBody RegisterRequest registerRequest) {
		if (registerRequest == null) {
			throw new ResultException(ErrorCode.NULL_ERROR);

		}
		validateRegistrationRequest(registerRequest);
		String register = userService.register(registerRequest);
		if (isNumeric(register)) {
			return ResultUtils.success(register);
		}
		return ResultUtils.failed(ErrorCode.PARAMS_ERROR, register);
	}

	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");
	}

	//endregion

	/**
	 * 删除接口
	 *
	 * @param user
	 * @param request
	 * @return
	 */

	@PostMapping("/delete")
	public CommonResult<Boolean> delete(@RequestBody User user, HttpServletRequest request) {
		if (!userService.isAdmin(request)) {
			throw new ResultException(ErrorCode.NOT_ADMIN);
		}

		if (user == null) {
			throw new ResultException(ErrorCode.NULL_ERROR);

		}
		boolean delete = userService.removeById(user);
		return ResultUtils.success(delete);
	}


	@GetMapping("/email")
	// 电话号API有点麻烦
	@ApiOperation(value = "通过邮箱查询用户")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "email", value = "邮箱")})
	public CommonResult<String> getUserByEmail(String email) {
		if (email == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
		userLambdaQueryWrapper.eq(User::getEmail, email);
		User user = userService.getOne(userLambdaQueryWrapper);
		if (user == null) {
			return ResultUtils.failed(ErrorCode.NULL_ERROR, "该账号未绑定邮箱");
		} else {
			String key = RedisConstants.USER_FORGET_PASSWORD_KEY + email;
			return ResultUtils.success(user.getUserAccount());
		}
	}

	@GetMapping("/check")
	@ApiOperation(value = "校验验证码")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "phone", value = "手机号"),
					@ApiImplicitParam(name = "code", value = "验证码")})
	public CommonResult<String> checkCode(String phone, String code) {
		String key = RedisConstants.USER_FORGET_PASSWORD_KEY + phone;
		String correctCode = (String) redisTemplate.opsForValue().get(key);
		if (correctCode == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "请先获取验证码");
		}
		if (!correctCode.equals(code)) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "验证码错误");
		}
		return ResultUtils.success("ok");
	}

	@PutMapping("/forget/update")
	@ApiOperation(value = "修改密码")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "updatePasswordRequest", value = "修改密码请求")})
	public CommonResult<String> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
		String email = updatePasswordRequest.getEmail();
		// String code = updatePasswordRequest.getCode();
		String password = updatePasswordRequest.getPassword();
		if (StringUtils.isAnyBlank(email, password)) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		userService.updatePassword(email, password);
		return ResultUtils.success("ok");
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
	public CommonResult<List<User>> searchUserByName(String username, HttpServletRequest request) {
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
	 *
	 */
	public CommonResult<Page<User>> recommend(long pageSize, long pageNum, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}


		//todo
		String rediskey = USER_RECOMMEND_KEY + loginUser.getId();
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
		System.out.println(page);
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
			throw new ResultException(ErrorCode.NOT_LOGIN);
//            return null;
		}
		Long id = currentUser.getId();
		User user = userService.getById(id);
		return ResultUtils.success(user);
	}


	/**
	 * 更新数据
	 *
	 * @param user
	 * @return
	 */

	@PostMapping("/update")
	public CommonResult<Integer> update(@Validated @RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
		if (userUpdateRequest == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		User user = new User();
		BeanUtils.copyProperties(userUpdateRequest, user);

		Integer result = userService.updateUser(user, request);
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

	/**
	 * 根据用户id查找用户
	 *
	 * @param id
	 * @return
	 */

	@GetMapping("/search/{id}")
	public CommonResult<UserVO> searchUserById(@PathVariable Long id, HttpServletRequest request) {
		if (id == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NULL_ERROR);
		}
		UserVO userVO = userService.searchUserById(id, loginUser);
		return ResultUtils.success(userVO);
	}

	@GetMapping("/match")
	public CommonResult<List<User>> matchUsers(long num, HttpServletRequest request) {
		if (num <= 0 || num > 20) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		return ResultUtils.success(userService.matchUsers(num, loginUser));

	}

}
