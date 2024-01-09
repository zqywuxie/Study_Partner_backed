package com.example.studypartner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.dto.UserDTO;
import com.example.studypartner.domain.entity.Follow;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.enums.RegisterStatus;
import com.example.studypartner.domain.request.RegisterRequest;
import com.example.studypartner.domain.vo.UserVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.mapper.UserMapper;
import com.example.studypartner.service.FollowService;
import com.example.studypartner.service.SysUserRoleService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.AlgorithmUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.studypartner.constant.EmailConstant.CAPTCHA_CACHE_KEY;
import static com.example.studypartner.constant.RedisConstants.USER_FORGET_PASSWORD_KEY;
import static com.example.studypartner.constant.RedisConstants.USER_RECOMMEND_KEY;
import static com.example.studypartner.constant.UserConstant.DEFAULT_AVATAR;
import static com.example.studypartner.constant.UserConstant.USER_LOGIN_STATUS;

/**
 * @author 思无邪
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createTime 2022-10-10 16:54:41
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
		implements UserService {

	//region 方法
	@Resource
	UserMapper userMapper;

	@Resource
	private RedisTemplate redisTemplate;


	@Resource
	private FollowService followService;

	@Resource
	private SysUserRoleService sysUserRoleService;

	//endregion

	//region 登录注册
	@Override
	public String register(RegisterRequest request) {
		// 提取请求参数
		String useraccount = request.getUseraccount();
		// 如果用户名为空，使用用户账号作为默认用户名
		String userName = StringUtils.defaultIfBlank(request.getUserName(), useraccount);
		String password = request.getPassword();
		String checkPassword = request.getCheckPassword();
		String email = request.getEmail();
		// 如果头像URL为空，使用默认头像URL
		String avatarUrl = StringUtils.defaultIfBlank(request.getAvatarUrl(), DEFAULT_AVATAR);
		String captcha = request.getCaptcha();

		// 从Redis缓存中获取验证码用于验证
		String checkCaptcha = (String) redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + email);

		// 验证码不匹配
		if (!StringUtils.equals(captcha, checkCaptcha)) {
			return "验证码不正确，请重新输入";
		}

		// 校验用户输入信息
		String validateResult = validateRegistrationInput(useraccount, password, checkPassword);
		if (StringUtils.isNotBlank(validateResult)) {
			return validateResult;
		}

		// 检查用户是否已存在
		if (userExists(useraccount, email)) {
			return RegisterStatus.REPEAT_ERROR.getText();
		}

		// 注册新用户
		User user = registerNewUser(useraccount, password, userName, email, avatarUrl);

		// 分配默认角色为游客
		sysUserRoleService.saveUserRoles(user.getId(), 3L);

		// 返回用户ID
		return String.valueOf(user.getId());
	}

	// 校验用户输入信息
	private String validateRegistrationInput(String useraccount, String password, String checkPassword) {
		// 信息不能为空
		if (StringUtils.isAnyBlank(useraccount, password, checkPassword)) {
			return ErrorCode.NULL_ERROR.getMessage();
		}

		// 账号大于等于6位
		if (useraccount.length() < 6) {
			return RegisterStatus.ACCOUNT_LEN.getText();
		}

		// 密码大于等于6位
		if (password.length() < 6) {
			return RegisterStatus.PASSWORD_LEN.getText();
		}

		// 密码与校验密码不一致
		if (!StringUtils.equals(password, checkPassword)) {
			return RegisterStatus.PASSWORD_CHECK.getText();
		}

		String specialCharPattern = "[!@#$%^&*()_+-=\\[\\]{};':\"\\\\|,.<>/?\\s]";
		// 账号不能包含特殊符号
		if (StringUtils.containsAny(useraccount, specialCharPattern)) {
			return RegisterStatus.ACCOUNT_PARAMS.getText();
		}

		return null; // 无校验错误
	}

	// 检查用户是否已存在
	private boolean userExists(String useraccount, String email) {
		LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(User::getUseraccount, useraccount).or().eq(User::getEmail, email);
		return userMapper.selectCount(wrapper) > 0;
	}

	// 注册新用户
	private User registerNewUser(String useraccount, String password, String userName, String email, String avatarUrl) {
		// 如果头像URL为空，使用默认头像URL
		if (Strings.isBlank(avatarUrl)) {
			avatarUrl = DEFAULT_AVATAR;
		}

		// 密码加密
		String encryptPassword = new BCryptPasswordEncoder().encode(password);

		// 插入用户数据
		User user = new User();
		user.setUseraccount(useraccount);
		user.setPassword(encryptPassword);
		user.setAvatarUrl(avatarUrl);
		user.setUsername(userName);
		user.setEmail(email);

		// 执行插入操作
		boolean saveResult = this.save(user);
		if (!saveResult) {
			throw new ResultException(ErrorCode.SYSTEM_ERROR, "用户注册失败");
		}

		return user;
	}


	@Override
	/**
	 * 用户登录逻辑
	 *
	 * @param useraccount 用户账号
	 * @param password   用户密码
	 * @param request    HTTP请求对象
	 * @return 登录成功的用户信息
	 */
	public User login(String useraccount, String password, HttpServletRequest request) {
		// 1.数据校验
		validateLoginData(useraccount, password);

		// 2.检查用户是否存在
		User user = getUserByAccount(useraccount);

		// 3.校验密码是否匹配
		validatePassword(password, user.getPassword());

		// 4.登录成功，清理用户敏感信息，设置Session
		User cleanUser = cleanUser(user);
		request.getSession().setAttribute(USER_LOGIN_STATUS, cleanUser);

		return cleanUser;
	}

	/**
	 * 数据校验：账号和密码不能为空，账号长度不能小于4位，密码长度不能小于6位，账号不能包含特殊符号
	 *
	 * @param useraccount 用户账号
	 * @param password    用户密码
	 */
	private void validateLoginData(String useraccount, String password) {
		if (StringUtils.isAnyBlank(useraccount, password)) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "账号和密码不能为空");
		}
		if (useraccount.length() < 6) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "账号长度不能小于4位");
		}
		if (password.length() < 6) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "密码长度不能小于6位");
		}
	}

	/**
	 * 根据用户账号获取用户信息
	 *
	 * @param useraccount 用户账号
	 * @return 用户信息
	 */
	private User getUserByAccount(String useraccount) {
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("useraccount", useraccount);
		User user = userMapper.selectOne(queryWrapper);
		if (user == null) {
			throw new ResultException(ErrorCode.NULL_ERROR, "用户不存在");
		}
		return user;
	}

	/**
	 * 校验密码是否匹配
	 *
	 * @param inputPassword  用户输入的密码
	 * @param storedPassword 存储的加密后的密码
	 */
	private void validatePassword(String inputPassword, String storedPassword) {
		boolean passwordMatches = new BCryptPasswordEncoder().matches(inputPassword, storedPassword);
		if (!passwordMatches) {
			log.info("登录失败，请检查账号和密码");
			throw new ResultException(ErrorCode.NULL_ERROR, "账号或密码错误");
		}
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


	//endregion

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
		// 用户信息脱敏，去掉一些敏感数据比如密码
		user.setId(user.getId());
		user.setUsername(user.getUsername());
		user.setUseraccount(user.getUseraccount());
		user.setAvatarUrl(user.getAvatarUrl());
		user.setGender(user.getGender());
		user.setEmail(user.getEmail());
		user.setStatus(user.getStatus());
		user.setPhone(user.getPhone());
		user.setCreateTime(user.getCreateTime());
		user.setProfile(user.getProfile());
		user.setFriendsIds(user.getFriendsIds());
		// 将数据通过session进行传入
		return user;
	}


	// region 更新查找方法
	@Override
	public Page<User> searchByText(UserDTO userDTO) {
		int pageSize = userDTO.getPageSize();
		int pageNum = userDTO.getPageNum();

		String searchText = userDTO.getSearchText();
		LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
		userQueryWrapper.like(User::getUsername, searchText)
				.or(u -> u.like(User::getProfile, searchText))
				.or(u -> u.like(User::getTags, searchText));
		List<User> list = this.list(userQueryWrapper);
		Page<User> page = this.page(new Page<>(pageNum, pageSize), userQueryWrapper);
		List<User> cleanList = list.stream().map(this::cleanUser).collect(Collectors.toList());
		page.setRecords(cleanList);
		return page;
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
	public void updatePassword(String email, String password) {
		String key = USER_FORGET_PASSWORD_KEY + email;
		LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
		userLambdaQueryWrapper.eq(User::getEmail, email);
		User user = this.getOne(userLambdaQueryWrapper);
		String encryptPassword = new BCryptPasswordEncoder().encode(password);
		user.setPassword(encryptPassword);
		this.updateById(user);
		redisTemplate.delete(key);
	}


	//endregion

	// region 根据id获得信息

	/**
	 * 根据用户ID查询用户信息，并判断与登录用户的关系（关注、好友）
	 *
	 * @param id        用户ID
	 * @param loginUser 登录用户
	 * @return 用户信息和关系
	 */
	@Override
	public UserVO searchUserById(Long id, User loginUser) {
		// 1. 获取用户信息
		User user = getUserById(id);

		// 2. 构建返回的用户视图对象
		UserVO userVO = buildUserVO(user);

		// 3. 判断与登录用户的关注关系
		checkFollowRelation(loginUser, id, userVO);

		// 4. 判断与登录用户的好友关系
		checkFriendRelation(loginUser, id, userVO);

		return userVO;
	}

	/**
	 * 获取用户信息
	 *
	 * @param id 用户ID
	 * @return 用户信息
	 */
	private User getUserById(Long id) {
		User user = this.getById(id);
		if (user == null) {
			throw new ResultException(ErrorCode.NULL_ERROR, "用户不存在");
		}
		return user;
	}

	/**
	 * 构建返回的用户视图对象
	 *
	 * @param user 用户信息
	 * @return 用户视图对象
	 */
	private UserVO buildUserVO(User user) {
		UserVO userVO = new UserVO();
		BeanUtils.copyProperties(user, userVO);
		return userVO;
	}

	/**
	 * 判断与登录用户的关注关系
	 *
	 * @param loginUser 登录用户
	 * @param id        要查询关系的用户ID
	 * @param userVO    用户视图对象
	 */
	private void checkFollowRelation(User loginUser, Long id, UserVO userVO) {
		LambdaQueryWrapper<Follow> followQueryWrapper = new LambdaQueryWrapper<>();
		followQueryWrapper.eq(Follow::getUserId, loginUser.getId()).eq(Follow::getFollowUserId, id);
		userVO.setIsFollow(followService.count(followQueryWrapper) > 0);
	}

	/**
	 * 判断与登录用户的好友关系
	 *
	 * @param loginUser 登录用户
	 * @param id        要查询关系的用户ID
	 * @param userVO    用户视图对象
	 */
	private void checkFriendRelation(User loginUser, Long id, UserVO userVO) {
		String friendsIds = StringUtils.trimToEmpty(loginUser.getFriendsIds());

		// Check if friendsIds is an empty array
		boolean contains = !StringUtils.equals("[]", friendsIds) && !StringUtils.equals("", friendsIds) &&
				Arrays.stream(StringUtils.strip(friendsIds, "[]").split(","))
						.map(String::trim)
						.map(Long::parseLong)
						.anyMatch(friendId -> friendId.equals(id));

		userVO.setIsFriend(contains);
	}


	// endregion


	@Override
	public User getLoginUser(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		Object attribute = request.getSession().getAttribute(USER_LOGIN_STATUS);
		return (User) attribute;
	}

	//region 管理员鉴权

	@Override
	public boolean isAdmin(HttpServletRequest request) {
		Object attribute = request.getSession().getAttribute(USER_LOGIN_STATUS);
		User user = (User) attribute;
		return user != null;
	}

	@Override
	public boolean isAdmin(User loginUser) {
		return loginUser != null;
	}


	//endregion


	//region 随机推荐
	@Override
	public Page<User> recommend(Long pageSize, Long currentPage, Long userId) {
		// 1. 从缓存获取推荐用户分页结果
		String redisKey = USER_RECOMMEND_KEY + userId;
		ValueOperations<String, Page<User>> valueOperations = redisTemplate.opsForValue();
		Page<User> userPage = valueOperations.get(redisKey);
		if (userPage != null) {
			return userPage;
		}

		// 2. 查询数据库获取推荐用户数据
		QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
		Page<User> page = this.page(new Page<>(currentPage, pageSize), userQueryWrapper);

		// 3. 去掉自己，并更新缓存
		List<User> records = page.getRecords();
		records.removeIf(user -> Objects.equals(user.getId(), userId));
		page.setRecords(records);

		try {
			// 设置缓存，并指定过期时间
			valueOperations.set(redisKey, page, 300, TimeUnit.SECONDS);
		} catch (Exception e) {
			log.error("Redis set key-value error", e);
		}

		return page;
	}

	//endregion

	//region 匹配机制
	//    todo 匹配,如何与前台分页进行使用

	/**
	 * 根据用户的标签匹配相似用户列表
	 *
	 * @param num       匹配用户数量
	 * @param loginUser 登录用户
	 * @return 匹配的用户列表
	 */

	@Override
	public List<User> matchUsers(long num, User loginUser) {
		// 1. 获取所有非空且不为"[]"的用户标签列表
		List<User> userList = getNonEmptyTagUsers();

		// 2. 获取登录用户的标签列表
		List<String> currentUserTagList = getCurrentUserTagList(loginUser);

		// 3. 计算相似度并排序
		List<Pair<User, Long>> sortedUserPairList = calculateAndSortSimilarity(userList, loginUser, currentUserTagList, num);

		// 4. 获取最终匹配的用户列表
		List<User> finalUserList = getFinalUserList(sortedUserPairList);

		return finalUserList;
	}

	/**
	 * 获取所有非空且不为"[]"的用户标签列表
	 *
	 * @return 用户列表
	 */
	private List<User> getNonEmptyTagUsers() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.select("id", "tags");
		queryWrapper.isNotNull("tags");
		queryWrapper.ne("tags", "[]");
		return this.list(queryWrapper);
	}

	/**
	 * 获取登录用户的标签列表
	 *
	 * @param loginUser 登录用户
	 * @return 标签列表
	 */
	private List<String> getCurrentUserTagList(User loginUser) {
		String tags = loginUser.getTags();
		if (StringUtils.isBlank(tags)) {
			return Collections.emptyList();
		}
		Gson gson = new Gson();
		return gson.fromJson(tags, new TypeToken<List<String>>() {
		}.getType());
	}

	/**
	 * 计算相似度并排序
	 *
	 * @param userList           用户列表
	 * @param loginUser          登录用户
	 * @param currentUserTagList 登录用户的标签列表
	 * @param num                匹配用户数量
	 * @return 排序后的用户列表
	 */
	private List<Pair<User, Long>> calculateAndSortSimilarity(List<User> userList, User loginUser,
															  List<String> currentUserTagList, long num) {
		User currentUser = this.getById(loginUser.getId());
		Gson gson = new Gson();
		List<Pair<User, Long>> list = new ArrayList<>();

		for (int i = 0; i < userList.size(); i++) {
			User user = userList.get(i);
			String userTags = user.getTags();
			if (StringUtils.isBlank(userTags) || user.getId().equals(currentUser.getId())) {
				continue;
			}
			List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
			}.getType());
			long distance = AlgorithmUtils.minDistance(currentUserTagList, userTagList);
			list.add(new Pair<>(user, distance));
		}

		return list.stream()
				.sorted(Comparator.comparingLong(Pair::getValue))
				.limit(num)
				.collect(Collectors.toList());
	}

	/**
	 * 获取最终匹配的用户列表
	 *
	 * @param sortedUserPairList 排序后的用户列表
	 * @return 最终匹配的用户列表
	 */
	private List<User> getFinalUserList(List<Pair<User, Long>> sortedUserPairList) {
		List<Long> userIdList = sortedUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
		QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
		userQueryWrapper.in("id", userIdList);
		Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
				.stream()
				.map(this::cleanUser)
				.collect(Collectors.groupingBy(User::getId));

		return userIdList.stream()
				.map(userId -> userIdUserListMap.get(userId).get(0))
				.collect(Collectors.toList());
	}

//endregion
}





