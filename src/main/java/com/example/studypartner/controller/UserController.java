package com.example.studypartner.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.constant.RedisConstants;
import com.example.studypartner.domain.dto.UserDTO;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.request.*;
import com.example.studypartner.domain.vo.UserLocationVO;
import com.example.studypartner.domain.vo.UserVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.service.SigninService;
import com.example.studypartner.service.UserLocationService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.List;

import static com.example.studypartner.constant.EmailConstant.CAPTCHA_CACHE_KEY;
import static com.example.studypartner.constant.UserConstant.USER_LOGIN_STATUS;

/**
 * @author wuxie
 * 用户数据接口
 */


@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"https://www.zqywuku.top/"}, allowCredentials = "true")
//@CrossOrigin(origins = {"http://localhost:5173"}, allowCredentials = "true")
@Slf4j
public class UserController {
	@Resource
	UserService userService;

	@Resource
	SigninService signinService;

	@Resource
	UserLocationService userLocationService;

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
	public CommonResult<User> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
		if (loginRequest == null) {
			throw new ResultException(ErrorCode.NULL_ERROR);
		}
		String useraccount = loginRequest.getUseraccount();
		String password = loginRequest.getPassword();
		if (StringUtils.isAllBlank(useraccount, password)) {
			throw new ResultException(ErrorCode.NULL_ERROR);
		}
		User user = userService.login(useraccount, password, request);
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
	public CommonResult<User> loginByEmail(@RequestBody LoginByEmailRequest loginByEmailRequest, HttpServletRequest request) {
		if (loginByEmailRequest == null) {
			return ResultUtils.failed(ErrorCode.NULL_ERROR);
		}
		String email = loginByEmailRequest.getEmail();
		String captcha = loginByEmailRequest.getCaptcha();
		String checkCaptcha = (String) redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + email);
		if (!captcha.equals(checkCaptcha)) {
			return ResultUtils.failed(ErrorCode.PARAMS_ERROR, "验证码不正确，请重新输入");
		}
		User user = userService.loginByEmail(email, request);
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
	 * 注册接口的数据验证方法
	 *
	 * @param registerRequest
	 */


	private void validateRegistrationRequest(RegisterRequest registerRequest) {
		if (registerRequest == null || StringUtils.isAnyBlank(
				registerRequest.getUseraccount(),
				registerRequest.getPassword(),
				registerRequest.getCheckPassword(),
				registerRequest.getEmail(),
				registerRequest.getCaptcha())) {
			throw new ResultException(ErrorCode.NULL_ERROR);
		}
		if (!registerRequest.getCheckPassword().equals(registerRequest.getPassword())) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
		}
	}

	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");
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

	//endregion


	//region 签到相关接口

	/**
	 * 签到接口
	 *
	 * @param request
	 * @return
	 */
	@PostMapping("/sign")
	public CommonResult<Boolean> sign(HttpServletRequest request) {
		Long loginId = userService.getLoginUser(request).getId();
		boolean flag = signinService.signIn(loginId);
		return ResultUtils.success(flag);
	}


	/**
	 * 连续签到天数
	 *
	 * @param request
	 * @return
	 */

	@GetMapping("/sign/count")
	public CommonResult<Integer> signDays(HttpServletRequest request) {
		Long loginId = userService.getLoginUser(request).getId();
		int signDays = signinService.signDays(loginId);
		return ResultUtils.success(signDays);
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
	@ApiOperation(value = "通过邮箱查询用户")
	@ApiImplicitParams(
			{@ApiImplicitParam(name = "email", value = "邮箱")})
	public CommonResult<String> getUserByEmail(@PathParam("email") String email) {
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
			return ResultUtils.success(user.getUseraccount());
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
		String password = updatePasswordRequest.getPassword();
		if (StringUtils.isAnyBlank(email, password)) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		userService.updatePassword(email, password);
		return ResultUtils.success("ok");
	}




	/**
	 * 根据用户id查询用户
	 *
	 * @param userDTO 用户id
	 * @return 用户
	 */
	@GetMapping("/searchByText")
	public CommonResult<Page<User>> searchByText(@RequestParam UserDTO userDTO) {
		return ResultUtils.success(userService.searchByText(userDTO));
	}

	/**
	 * 主页推荐用户
	 *
	 * @param pageSize
	 * @param currentPage
	 * @param request
	 * @return
	 */

	@GetMapping("/recommend")
	public CommonResult<Page<User>> recommend(@RequestParam Long pageSize, @RequestParam Long currentPage, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		return ResultUtils.success(userService.recommend(pageSize, currentPage, loginUser.getId()));
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
		User loginUser = userService.getById(userService.getLoginUser(request).getId());
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NULL_ERROR);
		}
		UserVO userVO = userService.searchUserById(id, loginUser);
		return ResultUtils.success(userVO);
	}

	@GetMapping("/match")
	public CommonResult<List<User>> matchUsers(long num, HttpServletRequest request) {
		if (num <= 0 || num > 20) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "查找数据过多");
		}
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		return ResultUtils.success(userService.matchUsers(num, loginUser));
	}


	//	region 地图匹配相关


	@GetMapping("/location")
	public CommonResult<List<UserLocationVO>> nearbyPartners(HttpServletRequest request, LocationRequest locationRequest) {
		Long loginUserId = userService.getLoginUser(request).getId();
		List<UserLocationVO> userLocationVOS = userLocationService.nearbyPartners(loginUserId, 1, locationRequest.getX(), locationRequest.getY());
		return ResultUtils.success(userLocationVOS);
	}
	//endregion
}
