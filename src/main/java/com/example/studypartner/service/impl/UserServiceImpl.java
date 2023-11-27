package com.example.studypartner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.studypartner.domain.entity.Follow;
import com.example.studypartner.domain.enums.RegisterStatus;
import com.example.studypartner.domain.request.RegisterRequest;
import com.example.studypartner.domain.vo.UserVO;
import com.example.studypartner.service.FollowService;
import com.example.studypartner.utils.ResultUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.mapper.UserMapper;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.AlgorithmUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.studypartner.constant.EmailConstant.CAPTCHA_CACHE_KEY;
import static com.example.studypartner.constant.RedisConstants.USER_FORGET_PASSWORD_KEY;
import static com.example.studypartner.constant.UserConstant.*;

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

	@Resource
	private RedisTemplate redisTemplate;

	@Resource
	private ObjectMapper objectMapper;

	@Resource
	private FollowService followService;

	// 加密盐值
	public static final String SALT = "zqy";


	@Override
	public String register(RegisterRequest request) {
		String userAccount = request.getUserAccount();
		String userName = request.getUserName();
		String userPassword = request.getUserPassword();
		String checkPassword = request.getCheckPassword();
		String email = request.getEmail();
		String avatarUrl = request.getAvatarUrl();
		String captcha = request.getCaptcha();
		String checkCaptcha = (String) redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + email);

		System.out.println(captcha + ":" + checkCaptcha);

		if (!captcha.equals(checkCaptcha)) {
			return "验证码不正确，请重新输入";
		}

		// 信息不能为空
		if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword, avatarUrl, userName)) {
			return ErrorCode.NULL_ERROR.getMessage();
		}
		// 账号大于6位
		if (userAccount.length() < 6) {
			return RegisterStatus.ACCOUNT_LEN.getText();
		}
		// 密码大于6位
		if (userPassword.length() < 6) {
			return RegisterStatus.PASSWORD_LEN.getText();
		}

		// 密码与校验密码一致不
		if (!userPassword.equals(checkPassword)) {
			return RegisterStatus.PASSWORD_CHECK.getText();
		}


		// 账号不能有特殊符号
		String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
		if (matcher.find()) {
			return RegisterStatus.ACCOUNT_PARAMS.getText();
		}


		LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(User::getUserAccount, userAccount)
				.or()
				.eq(User::getEmail, email);

		User user = userMapper.selectOne(wrapper);

		if (user != null) {
			// 处理用户已存在的情况
			return RegisterStatus.REPEAT_ERROR.getText();
		}

		// 如果用户不存在，继续执行代码


		if (Strings.isBlank(avatarUrl)) {
			avatarUrl = DEFAULT_AVATAR;
		}
		if (Strings.isBlank(userName)) {
			userName = userAccount;
		}


		// 密码加密
		String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
		// 插入数据
		user = new User();
		user.setUserAccount(userAccount);
		user.setUserPassword(encryptPassword);
		user.setAvatarUrl(avatarUrl);
		user.setUsername(userName);
		user.setAvatarUrl(avatarUrl);
		user.setEmail(email);
		boolean saveResult = this.save(user);
		if (!saveResult) {
			return ErrorCode.SYSTEM_ERROR.getMessage() + ":" + "插入";
		}
		return String.valueOf(user.getId());
	}

	@Override
	public User login(String userAccount, String userPassword, HttpServletRequest request) {
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
		User cleanUser = cleanUser(user);
//        String key = "currentUser";
//        String s = objectMapper.writeValueAsString(user);
//        stringRedisTemplate.opsForValue().set(key, s);
		request.getSession().setAttribute(USER_LOGIN_STATUS, cleanUser);
		return cleanUser;
	}


	@Override
	public User loginByEmail(String email, HttpServletRequest request) {
		LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
		userQueryWrapper.eq(User::getEmail, email);
		User user = userMapper.selectOne(userQueryWrapper);
		if (user == null) {
			throw new ResultException(ErrorCode.REPEAT_ERROR);
		}
		User cleanUser = cleanUser(user);
		request.getSession().setAttribute(USER_LOGIN_STATUS, cleanUser);
		return cleanUser;
	}

	/**
	 * 用户脱敏
	 *
	 * @param user
	 * @return
	 */
	@Override
	public User cleanUser(User user) {
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
		user.setFriendsIds(user.getFriendsIds());
		// 将数据通过session进行传入
		return user;
	}

	@Override
	public void updatePassword(String email, String password) {
		String key = USER_FORGET_PASSWORD_KEY + email;
		// String correctCode = stringRedisTemplate.opsForValue().get(key);
		// if (correctCode == null) {
		//     throw new BusinessException(ErrorCode.PARAMS_ERROR, "请先获取验证码");
		// }
		// if (!correctCode.equals(code)) {
		//     throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
		// }
		LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
		userLambdaQueryWrapper.eq(User::getEmail, email);
		User user = this.getOne(userLambdaQueryWrapper);
		String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
		user.setUserPassword(encryptPassword);
		this.updateById(user);
		redisTemplate.delete(key);
	}


	@Override
	public UserVO searchUserById(Long id, User loginUser) {
		User user = this.getById(id);
		UserVO uservo = new UserVO();
		BeanUtils.copyProperties(user, uservo);
		LambdaQueryWrapper<Follow> followQueryWrapper = new LambdaQueryWrapper<>();
		followQueryWrapper.eq(Follow::getUserId, loginUser.getId()).eq(Follow::getFollowUserId, id);
		long follow = followService.count(followQueryWrapper);
		uservo.setIsFollow(follow > 0);
		return uservo;
	}

	/**
	 * 根据标签搜索用户（Sql查询法）
	 *
	 * @param tagNameList
	 * @return
	 */


	@Override
	public List<User> searchUserByTags(List<String> tagNameList) {
		if (CollectionUtils.isEmpty(tagNameList)) {
			throw new ResultException(ErrorCode.NULL_ERROR);
		}

		QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
		int gender = -1;
		//and 语句
		for (String tagName : tagNameList) {
			if ("男".equals(tagName)) {
				gender = 0;
			} else if ("女".equals(tagName)) {
				gender = 1;
			}
			if (gender != -1) {
				userQueryWrapper = userQueryWrapper.eq("gender", gender);
				gender = -1;
			} else {
				userQueryWrapper = userQueryWrapper.like("tags", tagName);
			}
		}
		System.out.println(userQueryWrapper);
		List<User> users = userMapper.selectList(userQueryWrapper);
		return users.stream().map(this::cleanUser).collect(Collectors.toList());
	}

	/**
	 * 根据标签搜索用户 内存搜素法
	 *
	 * @param tagNameList
	 * @return
	 */

	@Override
	public List<User> memorySearch(List<String> tagNameList) {
		if (CollectionUtils.isEmpty(tagNameList)) {
			throw new ResultException(ErrorCode.NULL_ERROR);
		}
		QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
		List<User> users = userMapper.selectList(userQueryWrapper);
		Gson gson = new Gson();
		return users.stream().filter(user -> {
			String tags = user.getTags();
			Set<String> tempTagNameSet = gson.fromJson(tags, new TypeToken<Set<String>>() {
			}.getType());
			tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());

			for (String s : tempTagNameSet) {
				if (!tempTagNameSet.contains(s)) {
					return false;
				}
			}
			return true;

		}).map(this::cleanUser).collect(Collectors.toList());
	}

	@Override
	public Integer updateUser(User user, HttpServletRequest request) {
		//管理员可以更新任何用户信息
		//非管理员只能更新自己的数据

		User loginUser = this.getLoginUser(request);

		if (loginUser == null) {
			throw new ResultException(ErrorCode.NOT_LOGIN);
		}
		//todo 判断前端传入参数是否为空，是空那么就不进行  抛出异常
		if (!isAdmin(loginUser) && !user.getId().equals(loginUser.getId())) {
			throw new ResultException(ErrorCode.NOT_ADMIN);
		}
		User selectUser = userMapper.selectById(user.getId());
		if (selectUser == null) {
			throw new ResultException(ErrorCode.NULL_ERROR, "修改用户不存在");
		}
		return userMapper.updateById(user);
	}

	@Override
	public User getLoginUser(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		Object attribute = request.getSession().getAttribute(USER_LOGIN_STATUS);
		return (User) attribute;
	}

	/**
	 * 鉴权
	 *
	 * @param request
	 * @return
	 */

	@Override
	public boolean isAdmin(HttpServletRequest request) {
		Object attribute = request.getSession().getAttribute(USER_LOGIN_STATUS);
		User user = (User) attribute;
		return user != null || user.getUserRole().equals(ADMIN_ROLE);
	}

	@Override
	public boolean isAdmin(User loginUser) {
		return loginUser != null || loginUser.getUserRole().equals(ADMIN_ROLE);
	}

	//    todo 匹配
	@Override
	public List<User> matchUsers(long num, User loginUser) {
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.select("id", "tags");
		queryWrapper.isNotNull("tags");
		queryWrapper.ne("tags", "[]");
		List<User> userList = this.list(queryWrapper);
		String tags = loginUser.getTags();
		Gson gson = new Gson();


		// json 转 字符串
		List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
		}.getType());
		if (CollectionUtils.isEmpty(tagList)) {
			return new ArrayList<>();
		}
		// 用户列表的下标 => 相似度
		List<Pair<User, Long>> list = new ArrayList<>();
		// 依次计算所有用户和当前用户的相似度


		for (int i = 0; i < userList.size(); i++) {
			User user = userList.get(i);
			String userTags = user.getTags();
			// 无标签或者为当前用户自己
			if (StringUtils.isBlank(userTags) || user.getId().equals(loginUser.getId())) {
				continue;
			}
			List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
			}.getType());
			// 计算分数
			long distance = AlgorithmUtils.minDistance(tagList, userTagList);
			list.add(new Pair<>(user, distance));
		}


		// 按编辑距离由小到大排序
		List<Pair<User, Long>> topUserPairList = list.stream()
				.sorted((a, b) -> (int) (a.getValue() - b.getValue()))
				.limit(num)
				.collect(Collectors.toList());


		// 原本顺序的 userId 列表
		List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());

		QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
		userQueryWrapper.in("id", userIdList);
		// 1, 3, 2
		// User1、User2、User3
		// 1 => User1, 2 => User2, 3 => User3
		Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
				.stream()
				.map(this::cleanUser)
				.collect(Collectors.groupingBy(User::getId));


		List<User> finalUserList = new ArrayList<>();
		for (Long userId : userIdList) {
			finalUserList.add(userIdUserListMap.get(userId).get(0));
		}
		return finalUserList;
	}

}





