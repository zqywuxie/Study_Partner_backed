package com.example.studypartner.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.Team;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.entity.UserTeam;
import com.example.studypartner.domain.dto.TeamDTO;
import com.example.studypartner.domain.request.*;
import com.example.studypartner.domain.vo.TeamUserVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.service.TeamService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.service.UserTeamService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wuxie
 * 队伍接口
 */
@Api(value = "/team", tags = {"队伍接口"})
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

    @GetMapping("/list")
    public CommonResult<List<TeamUserVO>> searchAll(TeamDTO teamDTO, HttpServletRequest request) {
        if (teamDTO == null) {
            throw new ResultException(ErrorCode.PARAMS_ERROR, "数据为空");
        }
        User loginUser = userService.getLoginUser(request);
//        teamDTO.setUserId(loginUser.getId());
        List<TeamUserVO> teams = teamService.listTeams(teamDTO, true);
        queryTeamCount(request, teams);
        return ResultUtils.success(teams);
    }

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
        User loginUser = userService.getLoginUser(request);
        teamDTO.setUserId(loginUser.getId());
        List<TeamUserVO> teams = teamService.listTeams(teamDTO, true);
        queryTeamCount(request, teams);
        return ResultUtils.success(teams);
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
        User loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", loginUser);
        List<UserTeam> list = userTeamService.list(userTeamQueryWrapper);
        //去重 自己创建的用户
//        Map<Long, List<UserTeam>> collect = list.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
//        List<Long> idList = new ArrayList<>(collect.keySet());
        List<Long> idList = list.stream().map(UserTeam::getTeamId).collect(Collectors.toList());
//        teamDTO.setIdList(Collections.singletonList(loginUser.getId()));
//        teamDTO.setUserId(loginUser.getId());

        teamDTO.setJoinId(loginUserId);
        List<TeamUserVO> teamUserVOS = teamService.listTeams(teamDTO, true);
        queryTeamCount(request, teamUserVOS);
        return ResultUtils.success(teamUserVOS);
    }

    /**
     * 分页查询队伍
     *
     * @param teamDTO
     * @return
     */
    @GetMapping("/list/page")
    public CommonResult<Page<Team>> searchAllByPage(TeamDTO teamDTO) {
        if (teamDTO == null) {
            throw new ResultException(ErrorCode.SYSTEM_ERROR, "数据为空");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamDTO, team);
        Page<Team> page = new Page<>(teamDTO.getPageNum(), teamDTO.getPageSize());
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>(team);
        Page<Team> teamPage = teamService.page(page, teamQueryWrapper);
        return ResultUtils.success(teamPage);
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


    /**
     * 填充队伍人数字段
     *
     * @param request
     * @param teamList
     */
    private void queryTeamCount(HttpServletRequest request, List<TeamUserVO> teamList) {
        //条件查询出的队伍列表
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
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
        if (!CollectionUtils.isEmpty(teamIdList)) {
            QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
            userTeamJoinQueryWrapper.in("teamId", teamIdList);
            userTeamJoinList = userTeamService.list(userTeamJoinQueryWrapper);
        }

        //按每个队伍Id分组
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamJoinList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));

        teamList.forEach(team -> {
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
        });
    }
}
