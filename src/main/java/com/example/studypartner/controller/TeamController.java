package com.example.studypartner.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.Team;
import com.example.studypartner.domain.User;
import com.example.studypartner.domain.dto.TeamDTO;
import com.example.studypartner.domain.request.TeamAddInfo;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.service.TeamService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "long", name = "id", value = "", required = true)
    })
    @ApiOperation(value = "查询单个", notes = "查询单个", httpMethod = "GET")
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

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "TeamDTO", name = "teamDTO", value = "", required = true)
    })
    @ApiOperation(value = "获得全部数据", notes = "获得全部数据", httpMethod = "GET")
    @GetMapping("/list")
    public CommonResult<List<Team>> searchAll(TeamDTO teamDTO) {
        if (teamDTO == null) {
            throw new ResultException(ErrorCode.SYSTEM_ERROR, "数据为空");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamDTO, team);
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>(team);
        List<Team> teams = teamService.list(teamQueryWrapper);
        return ResultUtils.success(teams);
    }

    /**
     * @param teamDTO
     * @return
     */

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "TeamDTO", name = "teamDTO", value = "", required = true)
    })
    @ApiOperation(value = "", notes = "", httpMethod = "GET")
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


    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "TeamAddInfo", name = "teamAddInfo", value = "", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "request", value = "", required = true)
    })
    @ApiOperation(value = "", notes = "", httpMethod = "POST")
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

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "long", name = "id", value = "", required = true)
    })
    @ApiOperation(value = "", notes = "", httpMethod = "POST")
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

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "Team", name = "team", value = "", required = true)
    })
    @ApiOperation(value = "", notes = "", httpMethod = "POST")
    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody Team team) {
        if (team == null) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);
        }
        boolean save = teamService.updateById(team);
        if (!save) {
            throw new ResultException(ErrorCode.SYSTEM_ERROR, "更新错误");
        }
        return ResultUtils.success(true);
    }


}
