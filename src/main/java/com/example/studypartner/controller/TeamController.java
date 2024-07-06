package com.example.studypartner.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.Team;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.dto.TeamDTO;
import com.example.studypartner.domain.request.*;
import com.example.studypartner.domain.vo.TeamUserVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.service.TeamService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.service.UserTeamService;
import com.example.studypartner.utils.ResultUtils;
import jodd.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author wuxie
 * 队伍接口
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173/"})
@Slf4j
public class TeamController {

	@Resource
	private TeamService teamService;

	@Resource
	private UserService userService;

	@Resource
	private UserTeamService userTeamService;

	/**
	 * 查询单个队伍
	 *
	 * @param id
	 * @return
	 */

	@GetMapping("/get")
	public CommonResult<TeamUserVO> searchTeamByID(Long id, HttpServletRequest request) {
		if (id <= 0) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		TeamUserVO teamUserVo = teamService.getTeamById(id, true, loginUser);

		if (teamUserVo == null) {
			throw new ResultException(ErrorCode.NULL_ERROR);
		}
		return ResultUtils.success(teamUserVo);
	}


	/**
	 * 获得全部数据
	 *
	 * @return
	 */

//	@GetMapping("/list")
//	public CommonResult<List<TeamUserVO>> searchAll(TeamDTO teamDTO, HttpServletRequest request) {
//		if (teamDTO == null) {
//			throw new ResultException(ErrorCode.PARAMS_ERROR, "数据为空");
//		}
//		Long loginUserId = userService.getLoginUser(request).getId();
////        teamDTO.setUserId(loginUser.getId());
//		List<TeamUserVO> teams = teamService.listTeams(teamDTO, true);
//		teamService.queryTeamCount(loginUserId, teams);
//		return ResultUtils.success(teams);
//	}

	/**
	 * 获得当前用户创建的队伍
	 *
	 * @param teamDTO
	 * @param request
	 * @return
	 */
	@GetMapping("/my/create")
	public CommonResult<List<TeamUserVO>> myCreateTeams(TeamDTO teamDTO, HttpServletRequest request) {
		if (teamDTO == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "数据为空");
		}
		Long loginUserId = userService.getLoginUser(request).getId();
		List<TeamUserVO> teamUserVOS = teamService.myCreateTeams(teamDTO, loginUserId);
		return ResultUtils.success(teamUserVOS);
	}

	/**
	 * 当前用户加入的队伍
	 *
	 * @param teamDTO
	 * @param request
	 * @return
	 */
	@GetMapping("/my/join")
	public CommonResult<List<TeamUserVO>> myJoinTeams(TeamDTO teamDTO, HttpServletRequest request) {
		if (teamDTO == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "数据为空");
		}
		Long loginUserId = userService.getLoginUser(request).getId();
		List<TeamUserVO> teamUserVOS = teamService.myJoinTeams(teamDTO, loginUserId);
		return ResultUtils.success(teamUserVOS);
	}


	@GetMapping("/myTeam")
	public CommonResult<List<TeamUserVO>> getMyTeams(HttpServletRequest request) {
		Long loginUserId = userService.getLoginUser(request).getId();
		List<TeamUserVO> myCreateTeams = teamService.myCreateTeams(new TeamDTO(), loginUserId);
		List<TeamUserVO> myJoinTeams = teamService.myJoinTeams(new TeamDTO(), loginUserId);
		List<TeamUserVO> res = null;

		if (!CollectionUtils.isEmpty(myCreateTeams) && !CollectionUtils.isEmpty(myJoinTeams)) {
			res = new ArrayList<>(myCreateTeams);
			res.addAll(myJoinTeams);
		} else if (!CollectionUtils.isEmpty(myCreateTeams)) {
			res = myCreateTeams;
		} else {
			res = myJoinTeams;
		}
		return ResultUtils.success(res);
	}

	/**
	 * 分页查询队伍
	 *
	 * @param teamDTO
	 * @return
	 */
	@GetMapping("/list/page")
	public CommonResult<Page<TeamUserVO>> searchAllByPage(HttpServletRequest request, TeamDTO teamDTO) {
		if (teamDTO == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR, "数据为空");
		}
		Long loginUserId = userService.getLoginUser(request).getId();
		Page<TeamUserVO> teamUserVOS = teamService.searchAllByPage(teamDTO, loginUserId);
		return ResultUtils.success(teamUserVOS);
	}

	/**
	 * 添加队伍
	 *
	 * @param teamAddRequest
	 * @param request
	 * @return
	 */


	//todo 添加失败
	@PostMapping("/add")
	public CommonResult<Long> add(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
		if (teamAddRequest == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		Team team = new Team();
		BeanUtils.copyProperties(teamAddRequest, team);
		long teamId = teamService.addTeam(team, loginUser);
		return ResultUtils.success(teamId);
	}

	/**
	 * 删除队伍
	 *
	 * @param id
	 * @return
	 */

	@PostMapping("/delete")
	public CommonResult<Boolean> delete(@RequestBody TeamDeleteRequest teamDeleteRequest, HttpServletRequest request) {
		if (teamDeleteRequest == null || teamDeleteRequest.getTeamId() <= 0) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		Long teamId = teamDeleteRequest.getTeamId();
		User loginUser = userService.getLoginUser(request);
		boolean result = teamService.deleteTeam(teamId, loginUser);
		return ResultUtils.success(result);
	}

	/**
	 * 更新队伍
	 *
	 * @param teamUpdateRequest
	 * @return
	 */

	@PostMapping("/update")
	public CommonResult<Boolean> update(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
		if (teamUpdateRequest == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		boolean save = teamService.updateTeam(teamUpdateRequest, loginUser);
		if (!save) {
			throw new ResultException(ErrorCode.SYSTEM_ERROR, "更新错误");
		}
		return ResultUtils.success(true);
	}

	@PostMapping("/join")
	public CommonResult<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
		if (teamJoinRequest == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
		return ResultUtils.success(result);

	}


	@PostMapping("/quit")
	public CommonResult<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
		if (teamQuitRequest == null) {
			throw new ResultException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		Boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
		return ResultUtils.success(result);
	}


}
