package com.example.studypartner.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.Team;
import com.example.studypartner.domain.User;
import com.example.studypartner.domain.dto.TeamDTO;
import com.example.studypartner.domain.request.TeamAddInfo;
import com.example.studypartner.domain.request.TeamJoinInfo;
import com.example.studypartner.domain.request.TeamUpdateInfo;
import com.example.studypartner.domain.vo.TeamUserVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.service.TeamService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    /**
     * 查询单个
     *
     * @param id
     * @return
     */

    @GetMapping("/get")
    public CommonResult<Team> searchOne(@RequestBody Long id) {
        if (id <= 0) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new ResultException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(team);
    }

    /**
     * 获得全部数据
     *
     * @return
     */

    @GetMapping("/list")
    public CommonResult<List<TeamUserVO>> searchAll(TeamDTO teamDTO, HttpServletRequest request) {
        if (teamDTO == null) {
            throw new ResultException(ErrorCode.SYSTEM_ERROR, "数据为空");
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teams = teamService.listTeams(teamDTO, isAdmin);
        return ResultUtils.success(teams);
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
     * @param teamAddInfo
     * @param request
     * @return
     */


    @PostMapping("/add")
    public CommonResult<Long> add(@RequestBody TeamAddInfo teamAddInfo, HttpServletRequest request) {
        if (teamAddInfo == null) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddInfo, team);
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
    public CommonResult<Boolean> delete(@RequestBody Long id) {
        if (id <= 0) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);
        }
        boolean save = teamService.removeById(id);
        if (!save) {
            throw new ResultException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新队伍
     *
     * @param teamUpdateInfo
     * @return
     */

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody TeamUpdateInfo teamUpdateInfo, HttpServletRequest request) {
        if (teamUpdateInfo == null) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean save = teamService.updateTeam(teamUpdateInfo, loginUser);
        if (!save) {
            throw new ResultException(ErrorCode.SYSTEM_ERROR, "更新错误");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/join")
    public CommonResult<Boolean> joinTeam(@RequestBody TeamJoinInfo teamJoinInfo,HttpServletRequest request) {
        if (teamJoinInfo == null) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinInfo,loginUser);
        return ResultUtils.success(result);

    }


}
