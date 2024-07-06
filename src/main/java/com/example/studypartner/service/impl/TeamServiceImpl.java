package com.example.studypartner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.Team;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.entity.UserTeam;
import com.example.studypartner.domain.dto.TeamDTO;
import com.example.studypartner.domain.enums.TeamStatus;
import com.example.studypartner.domain.request.TeamJoinRequest;
import com.example.studypartner.domain.request.TeamQuitRequest;
import com.example.studypartner.domain.request.TeamUpdateRequest;
import com.example.studypartner.domain.vo.TeamUserVO;
import com.example.studypartner.domain.vo.UserVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.mapper.TeamMapper;
import com.example.studypartner.service.TeamService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.service.UserTeamService;
import com.example.studypartner.utils.ResultUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author wuxie
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2023-02-03 11:45:44
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
		implements TeamService {
	@Resource
	private UserTeamService userTeamService;

	@Resource
	private UserService userService;

	@Resource
	private RedissonClient redissonClient;


	//region 增删改查

	/**
	 * todo 这样整个方法都进入了事务 待优化
	 *
	 * @param team
	 * @param loginUser
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public long addTeam(Team team, User loginUser) {
		validateInputParameters(team, loginUser);

		long userId = loginUser.getId();

		// 验证用户创建队伍数量
		validateUserTeamCount(userId);

		// 设置队伍信息
		setTeamInformation(team, userId);

		// 存储队伍信息到数据库
		boolean saveTeam = this.save(team);
		if (!saveTeam) {
			throw new ResultException(ErrorCode.SYSTEM_ERROR, "插入team表失败");
		}

		// 在映射表中插入记录
		Long teamId = team.getId();
		saveUserTeamMapping(userId, teamId);

		return teamId;
	}

	/**
	 * 验证输入参数的有效性
	 *
	 * @param team      队伍信息
	 * @param loginUser 登录用户
	 */
	private void validateInputParameters(Team team, User loginUser) {
		if (team == null || loginUser == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}

		String name = team.getName();
		int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
		String description = team.getDescription();
		int status = Optional.ofNullable(team.getStatus()).orElse(-1);
		TeamStatus teamStatus = TeamStatus.getTeamStatus(status);
		String password = team.getPassword();
		Date expireTime = team.getExpireTime();

		if (StringUtils.isBlank(name) || name.length() > 20 ||
				maxNum < 1 || maxNum > 20 ||
				(StringUtils.isNotBlank(description) && description.length() > 512) ||
				teamStatus == null ||
				(TeamStatus.PRIVATE.equals(teamStatus) && (StringUtils.isBlank(password) || password.length() > 32)) ||
				new Date().after(expireTime)) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "参数不满足要求");
		}
	}

	/**
	 * 验证用户创建队伍数量
	 *
	 * @param userId 用户ID
	 */
	private void validateUserTeamCount(long userId) {
		QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
		teamQueryWrapper.eq("user_id", userId);
		long teamCount = this.count(teamQueryWrapper);
		if (teamCount >= 5) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "队伍过多");
		}
	}

	/**
	 * 设置队伍信息
	 *
	 * @param team   队伍信息
	 * @param userId 用户ID
	 */
	private void setTeamInformation(Team team, long userId) {
		team.setUserId(userId);
	}

	/**
	 * 在映射表中插入记录
	 *
	 * @param userId 用户ID
	 * @param teamId 队伍ID
	 */
	private void saveUserTeamMapping(long userId, Long teamId) {
		UserTeam userTeam = new UserTeam();
		userTeam.setTeamId(teamId);
		userTeam.setUserId(userId);
		userTeam.setJoinTime(new Date());
		boolean result = userTeamService.save(userTeam);
		if (!result) {
			throw new ResultException(ErrorCode.SYSTEM_ERROR, "插入user_team表失败");
		}
	}


	@Override
	/**
	 删除动作开始前记得添加事务
	 todo 没有判断是否为管理员,前台也只有管理员才能看到删除按钮
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public boolean deleteTeam(Long id, User loginUser) {
		Team team = getTeamById(id);
		Long teamId = team.getId();
		if (!team.getUserId().equals(loginUser.getId())) {
			throw new ResultException(ErrorCode.NOT_ADMIN, "无访问权限");
		}
		QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
		userTeamQueryWrapper.eq("team_id", teamId);
		boolean remove = userTeamService.remove(userTeamQueryWrapper);
		if (!remove) {
			throw new ResultException(ErrorCode.SYSTEM_ERROR, "解散队伍失败");
		}
		return this.removeById(teamId);
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
		// 校验输入参数
		validateQuitTeamInput(teamQuitRequest);

		Long teamId = teamQuitRequest.getTeamId();
		Team team = getTeamById(teamId);

		// 判断用户是否加入队伍
		Long userId = loginUser.getId();
		validateUserInTeam(teamId, userId);

		long userCount = getUserCount(teamId);

		// 如果队伍只剩一人，解散队伍
		if (userCount == 1) {
			return dissolveTeam(teamId);
		} else {
			// 如果该用户是队长
			if (team.getUserId().equals(userId)) {
				reassignTeamLeader(teamId);
			}
		}

		// 移除用户与队伍的关联
		return removeUserFromTeam(teamId, userId);
	}

	// 校验退出队伍的输入参数
	private void validateQuitTeamInput(TeamQuitRequest teamQuitRequest) {
		if (teamQuitRequest == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
	}


	// 校验用户是否加入队伍，如果未加入则抛出异常
	private void validateUserInTeam(Long teamId, Long userId) {
		UserTeam userTeam = new UserTeam();
		userTeam.setTeamId(teamId);
		userTeam.setUserId(userId);
		QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>(userTeam);
		long count = userTeamService.count(userTeamQueryWrapper);
		if (count == 0) {
			throw new ResultException(ErrorCode.NULL_ERROR, "用户未加入该队伍");
		}
	}

	// 获取队伍成员数量
	private long getUserCount(Long teamId) {
		QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
		userTeamQueryWrapper.eq("team_id", teamId);
		return userTeamService.count(userTeamQueryWrapper);
	}

	// 解散队伍
	private Boolean dissolveTeam(Long teamId) {
		return this.removeById(teamId);
	}

	// 更换队伍队长
	private void reassignTeamLeader(Long teamId) {
		QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
		userTeamQueryWrapper.eq("team_id", teamId);
		userTeamQueryWrapper.last("order by id asc limit 2");
		List<UserTeam> list = userTeamService.list(userTeamQueryWrapper);
		if (CollectionUtils.isEmpty(list) || list.size() <= 1) {
			throw new ResultException(ErrorCode.SYSTEM_ERROR);
		}
		UserTeam nextUserTeam = list.get(1);
		Long nextUserTeamUserId = nextUserTeam.getUserId();

		// 更新队伍信息，将下一位成员设为队长
		Team updateTeam = new Team();
		updateTeam.setUserId(nextUserTeamUserId);
		updateTeam.setId(teamId);
		boolean result = this.updateById(updateTeam);
		if (!result) {
			throw new ResultException(ErrorCode.SYSTEM_ERROR, "更新队长失败");
		}
	}

	// 移除用户与队伍的关联
	private Boolean removeUserFromTeam(Long teamId, Long userId) {
		QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
		userTeamQueryWrapper.eq("team_id", teamId);
		userTeamQueryWrapper.eq("user_id", userId);
		return userTeamService.remove(userTeamQueryWrapper);
	}

	/**
	 * todo **关联查询已加入队伍的用户信息（可能会很耗费性能，建议大家用自己写 SQL 的方式实现）**
	 */
	@Override
	public List<TeamUserVO> listTeams(TeamDTO teamDTO, boolean isAdmin) {
		// 构建查询条件
		QueryWrapper<Team> teamQueryWrapper = buildTeamQueryWrapper(teamDTO, isAdmin);

		// 查询队伍列表
		List<Team> teamList = this.list(teamQueryWrapper);
		if (CollectionUtils.isEmpty(teamList)) {
			return new ArrayList<>();
		}

		// 构建 TeamUserVO 列表
		return buildTeamUserVOList(teamList);
	}

	// 构建查询条件
	private QueryWrapper<Team> buildTeamQueryWrapper(TeamDTO teamDTO, boolean isAdmin) {
		QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
		if (teamDTO != null) {
			// 根据 TeamDTO 的字段构建查询条件
			// ...
			String description = teamDTO.getDescription();
			if (StringUtils.isNotBlank(description)) {
				teamQueryWrapper.eq("description", description);
			}
			String name = teamDTO.getName();
			if (StringUtils.isNotBlank(name)) {
				teamQueryWrapper.eq("name", name);
			}
			//查看队伍的类型
			Integer status = teamDTO.getStatus();
			List<Long> list = Arrays.asList(0L, 1L, 2L);
			TeamStatus teamStatus = null;
			if (isAdmin) {
				teamQueryWrapper.in("status", list);
			} else {
				if (status == null) {
					status = 0;
				}
				teamStatus = TeamStatus.getTeamStatus(status);
				if (teamStatus == null) {
					teamStatus = TeamStatus.PUBLIC;
				}
				teamQueryWrapper.eq("status", teamStatus.getValue());
			}


			Integer maxNum = teamDTO.getMaxNum();
			if (maxNum != null && maxNum <= 5) {
				teamQueryWrapper.eq("max_num", maxNum);
			}

			Long userId = teamDTO.getUserId();
			if (userId != null && userId > 0) {
				teamQueryWrapper.eq("user_id", userId);
			}

			String searchText = teamDTO.getSearchText();
			if (StringUtils.isNotBlank(searchText)) {
				teamQueryWrapper.and(tq -> tq.like("name", searchText).or().like("description", searchText));
			}


			List<Long> idList = teamDTO.getIdList();
			if (!CollectionUtils.isEmpty(idList)) {
				teamQueryWrapper.in("id", idList);
			}

//todo  ?
//			if (!isAdmin && teamStatus != TeamStatus.PUBLIC) {
//				throw new ResultException(ErrorCode.NOT_ADMIN);
//			}


		}

		// 添加过期时间的查询条件
		teamQueryWrapper.and(tq -> tq.gt("expire_time", new Date()).or().isNull("expire_time"));

		return teamQueryWrapper;
	}

	// 构建 TeamUserVO 列表
	private List<TeamUserVO> buildTeamUserVOList(List<Team> teamList) {
		List<TeamUserVO> teamUserVOS = new ArrayList<>();
		for (Team team : teamList) {
			Long createUserId = team.getUserId();
			if (createUserId == null) {
				continue;
			}
			User user = userService.getById(createUserId);
			List<UserVO> manageUser = new ArrayList<>();
			TeamUserVO teamUserVO = new TeamUserVO();
			BeanUtils.copyProperties(team, teamUserVO);
			if (user != null) {
				UserVO userVO = new UserVO();
				BeanUtils.copyProperties(user, userVO);
				//todo 后序添加管理员ID
				manageUser.add(userVO);
				teamUserVO.setManageUserList(manageUser);
			}
			teamUserVOS.add(teamUserVO);
		}
		return teamUserVOS;
	}


	@Override
	public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
		if (teamUpdateRequest == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		Long id = teamUpdateRequest.getId();
		if (id == null || id <= 0) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		Team team = this.getById(id);
		if (team == null) {
			throw new ResultException(ErrorCode.NULL_ERROR);
		}
		boolean admin = userService.isAdmin(loginUser);
		if (!admin && !team.getUserId().equals(loginUser.getId())) {
			throw new ResultException(ErrorCode.NOT_ADMIN);
		}
		//队伍状态改为加密需要增加密码
		Integer status = teamUpdateRequest.getStatus();
		String password = teamUpdateRequest.getPassword();
		TeamStatus teamStatus = TeamStatus.getTeamStatus(status);
		//todo  原本加密改为加密后的优化
		if (teamStatus == TeamStatus.PRIVATE) {
			if (StringUtils.isBlank(password)) {
				throw new ResultException(ErrorCode.NULL_ERROR, "加密房间必须要有密码");
			}
		}

		Team team1 = new Team();
		BeanUtils.copyProperties(teamUpdateRequest, team1);
		return this.updateById(team1);
	}


	//endregion


	//region 加入队伍
	//    todo 加队伍
	public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
		if (teamJoinRequest == null) {
			throw new ResultException(ErrorCode.NULL_ERROR);
		}

		Long teamId = teamJoinRequest.getTeamId();
		Team team = this.getById(teamId);

		validateJoinTeam(team);
		validateTeamExpiration(team);

		Long loginUserId = loginUser.getId();
		RLock lock = redissonClient.getLock("zqy:join_tem");

		try {
			while (true) {
				if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
					validateUserTeamCount(loginUserId);
					validateTeamFull(team);
					validateUserNotJoined(teamId, loginUserId);

					UserTeam userTeam = new UserTeam();
					userTeam.setTeamId(teamId);
					userTeam.setUserId(loginUserId);
					userTeam.setJoinTime(new Date());
					return userTeamService.save(userTeam);
				}
			}
		} catch (InterruptedException e) {
			log.error("joinTeam error", e);
			return false;
		} finally {
			// 使用 try-with-resources 简化资源管理
			try {
				if (lock.isHeldByCurrentThread()) {
					System.out.println(Thread.currentThread().getId() + " unlock success");
				}
			} finally {
				lock.unlock();
			}
		}
	}

	private void validateJoinTeam(Team team) {
		if (team == null) {
			throw new ResultException(ErrorCode.NULL_ERROR, "加入队伍不存在");
		}

		TeamStatus teamStatus = TeamStatus.getTeamStatus(team.getStatus());
		if (TeamStatus.BANDED.equals(teamStatus)) {
			throw new ResultException(ErrorCode.NULL_ERROR, "该队伍已被封禁");
		}

		validateTeamPassword(team, team.getPassword());
	}

	private void validateTeamPassword(Team team, String password) {
		TeamStatus teamStatus = TeamStatus.getTeamStatus(team.getStatus());
		if (TeamStatus.PRIVATE.equals(teamStatus) && !password.equals(team.getPassword())) {
			throw new ResultException(ErrorCode.NULL_ERROR, "密码不正确");
		}
	}

	private void validateTeamExpiration(Team team) {
		if (team.getExpireTime().before(new Date())) {
			throw new ResultException(ErrorCode.NULL_ERROR, "队伍已过期");
		}
	}

	private void validateUserTeamCount(Long loginUserId) {
		QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
		userTeamQueryWrapper.eq("user_id", loginUserId);
		long count = userTeamService.count(userTeamQueryWrapper);
		if (count >= 5) {
			throw new ResultException(ErrorCode.INSERT_ERROR, "用户加入队伍过多,最多加入5个队伍");
		}
	}

	private void validateTeamFull(Team team) {
		long userCount = getUserCount(team.getId());
		if (userCount >= team.getMaxNum()) {
			throw new ResultException(ErrorCode.NULL_ERROR, "队伍已满");
		}
	}

	private void validateUserNotJoined(Long teamId, Long loginUserId) {
		QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
		userTeamQueryWrapper.eq("team_id", teamId);
		userTeamQueryWrapper.eq("user_id", loginUserId);
		long hasUserJoin = userTeamService.count(userTeamQueryWrapper);
		if (hasUserJoin > 0) {
			throw new ResultException(ErrorCode.NULL_ERROR, "用户已加入队伍");
		}
	}


	//endregion

	/**
	 * 根据id 获取队伍
	 *
	 * @param teamId
	 * @return
	 */

	//todo
	private Team getTeamById(Long teamId) {
		if (teamId == null || teamId <= 0) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		Team team = this.getById(teamId);
		if (team == null) {
			throw new ResultException(ErrorCode.NULL_ERROR, "队伍不存在");
		}
		return team;
	}


	@Override
	public TeamUserVO getTeamById(long id, boolean isAdmin, User loginUser) {
		validateTeamId(id);

		Team team = this.getById(id);
		Long teamId = team.getId();
		Long userId = team.getUserId();

		TeamUserVO teamUserVo = new TeamUserVO();
		BeanUtils.copyProperties(team, teamUserVo);

		List<UserTeam> userTeamList = getUserTeamList(teamId);

		teamUserVo.setHasJoinNum(userTeamList.size());

		List<UserVO> manageUser = new ArrayList<>();
		List<UserVO> memberUser = new ArrayList<>();

		boolean userAlreadyJoined = setUserListsAndCheckIfUserJoined(userId, loginUser, userTeamList, manageUser, memberUser, teamUserVo);

		validatePermissions(team.getStatus(), teamUserVo.isHasJoin(), isAdmin, userAlreadyJoined);

		teamUserVo.setManageUserList(manageUser);
		teamUserVo.setJoinUserList(memberUser);

		return teamUserVo;
	}

	//左外连接

	@Override
	public List<TeamUserVO> myJoinTeams(TeamDTO teamDTO, Long loginUserId) {
		// 从数据库中获取Team数据
		if (teamDTO.getUserId() != null) {
			loginUserId = teamDTO.getUserId();
		}
		LambdaQueryWrapper<Team> teamQuery = new LambdaQueryWrapper<Team>().eq(Team::getUserId, loginUserId);
		List<Team> teams = this.list(teamQuery);

		// 从数据库中获取UserTeam数据
		LambdaQueryWrapper<UserTeam> userTeamsQuery = new LambdaQueryWrapper<UserTeam>().eq(UserTeam::getUserId, loginUserId);
		List<UserTeam> userTeams = userTeamService.list(userTeamsQuery);

		// 创建一个Set来存储Team的id和user_id，以便快速查找
		Set<Long> teamIds = teams.stream()
				.map(Team::getId) // 获取Team的id
				.collect(Collectors.toSet());
		Set<Long> teamUserIds = teams.stream()
				.map(team -> team.getUserId()) // 获取Team的user_id
				.collect(Collectors.toSet());

		// 使用Java 8的流API过滤掉满足特定条件的userTeams条目
		List<Long> filteredUserTeams = userTeams.stream()
				.filter(userTeam -> !teamIds.contains(userTeam.getTeamId()) && // 检查team_id是否存在于Team的id集合中
						!teamUserIds.contains(userTeam.getUserId())) // 检查user_id是否存在于Team的user_id集合中
				.map(UserTeam::getTeamId).collect(Collectors.toList()); // 将过滤后的结果收集到一个新的列表中

		if (filteredUserTeams.isEmpty()) {
			return null;
		}
		teamDTO.setIdList(filteredUserTeams);
		List<TeamUserVO> teamUserVOS = this.listTeams(teamDTO, true);
		return teamUserVOS;
	}

	@Override
	public List<TeamUserVO> myCreateTeams(TeamDTO teamDTO, Long loginUserId) {
		if (teamDTO.getUserId() != null) {
			loginUserId = teamDTO.getUserId();
		}
		LambdaQueryWrapper<Team> select = new LambdaQueryWrapper<Team>().eq(Team::getUserId, loginUserId).select(Team::getId);
		List<Long> idList = this.list(select).stream().map(Team::getId).collect(Collectors.toList());
		teamDTO.setIdList(idList);
		List<TeamUserVO> teams = this.listTeams(teamDTO, true);
		queryTeamCount(loginUserId, teams);
		return teams;
	}

	@Override
	public Page<TeamUserVO> searchAllByPage(TeamDTO teamDTO, Long loginUserId) {
		teamDTO.setUserId(loginUserId);
		List<TeamUserVO> teams = this.listTeams(teamDTO, false);
		this.queryTeamCount(loginUserId, teams);
		Page<TeamUserVO> teamUserVOPage = new Page<>(teamDTO.getPageNum(), teamDTO.getPageSize());
		teamUserVOPage.setRecords(teams);
		return teamUserVOPage;
	}


	/**
	 * 填充队伍人数字段
	 *
	 * @param request
	 * @param teamList
	 */
	private void queryTeamCount(Long loginUserId, List<TeamUserVO> teamList) {
		//条件查询出的队伍列表
		List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
		QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
		try {
			userTeamQueryWrapper.eq("user_id", loginUserId);
			userTeamQueryWrapper.in("team_id", teamIdList);
			//已加入队伍集合
			List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
			//已加入的队伍的id集合
			Set<Long> hasJoinTeamIdList = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
			teamList.forEach(team -> {
				boolean hasJoin = hasJoinTeamIdList.contains(team.getId());
				team.setHasJoin(hasJoin);
			});
		} catch (Exception e) {
		}

		List<UserTeam> userTeamJoinList = new ArrayList<>();
		if (!com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(teamIdList)) {
			QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
			userTeamJoinQueryWrapper.in("team_id", teamIdList);
			userTeamJoinList = userTeamService.list(userTeamJoinQueryWrapper);
		}

		//按每个队伍Id分组
		Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamJoinList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));

		teamList.forEach(team -> {
			team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
		});
	}


	private void validateTeamId(long id) {
		if (id <= 0) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "队伍ID不合法");
		}
	}

	private List<UserTeam> getUserTeamList(Long teamId) {
		QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
		userTeamQueryWrapper.eq("team_id", teamId);
		return userTeamService.list(userTeamQueryWrapper);
	}

	private boolean setUserListsAndCheckIfUserJoined(Long userId, User loginUser, List<UserTeam> userTeamList, List<UserVO> manageUser, List<UserVO> memberUser, TeamUserVO teamUserVo) {
		boolean userAlreadyJoined = false;

		for (UserTeam userTeam : userTeamList) {
			Long memberId = userTeam.getUserId();
			User user = userService.getById(memberId);
			UserVO userVo = new UserVO();
			BeanUtils.copyProperties(user, userVo);

			if (memberId.equals(userId)) {
				manageUser.add(userVo);
				userAlreadyJoined = true;
			} else {
				memberUser.add(userVo);
			}

			if (loginUser.getId().equals(memberId)) {
				teamUserVo.setHasJoin(true);
			}
		}
		return userAlreadyJoined;
	}

	private void validatePermissions(int teamStatus, boolean hasJoin, boolean isAdmin, boolean userAlreadyJoined) {
		if (!(teamStatus == TeamStatus.PUBLIC.getValue()) && !hasJoin && !isAdmin && !userAlreadyJoined) {
			throw new ResultException(ErrorCode.NOT_ADMIN, "无权限");
		}
	}


}




