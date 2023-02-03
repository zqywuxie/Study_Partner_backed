package com.example.studypartner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.Team;
import com.example.studypartner.domain.User;
import com.example.studypartner.domain.UserTeam;
import com.example.studypartner.domain.enums.TeamStatus;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.service.TeamService;
import com.example.studypartner.mapper.TeamMapper;
import com.example.studypartner.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

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

    @Override
    //插入数据前设置事务
    @Transactional(rollbackFor = Exception.class)
    /**
     todo 这样导致整个方法都进入了事务,后续优化
     */
    public long addTeam(Team team, User loginUser) {
        final long userId = loginUser.getId();
        if (team == null) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);
        }
        if (loginUser == null) {
            throw new ResultException(ErrorCode.NO_LOGIN);
        }
        /**
         * - 队伍名称  <=20
         * - 队伍人数>1&&<=20
         * - 队伍描述 <=512
         * - 队伍状态 （默认公开 0） ，传入1为私人，传入3为加密房间（必须有密码）
         * - 超时时间>当前时间
         * - 用户创建队伍 <5
         */

        //队伍名称  <=20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new ResultException(ErrorCode.PARAMS_ERROR, "队伍名称过长");
        }
        //队伍人数>1&&<=20
        //判断是否为空,是就默认值设为0
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new ResultException(ErrorCode.PARAMS_ERROR, "队伍人数无效");
        }
        //队伍描述 <=512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new ResultException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }

        //队伍状态 （默认公开 0） ，传入1为私人，传入3为加密房间（必须有密码）
        int status = Optional.ofNullable(team.getStatus()).orElse(-1);
        TeamStatus teamStatus = TeamStatus.getTeamStatus(status);
        if (teamStatus == null) {
            throw new ResultException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        String password = team.getPassword();
        if (TeamStatus.SECRET.equals(teamStatus) && StringUtils.isBlank(password) || password.length() > 32) {
            throw new ResultException(ErrorCode.PARAMS_ERROR, "队伍密码设置有误");
        }

        //超时时间>当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new ResultException(ErrorCode.PARAMS_ERROR, "超时时间已结束");
        }

        //用户创建队伍 <5
        //todo bug 同时点击 会导致问题
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("userId", userId);
        long count = this.count(teamQueryWrapper);
        if (count >= 5) {
            throw new ResultException(ErrorCode.PARAMS_ERROR, "队伍过多");
        }

        //将队伍存储到数据库
        team.setUserId(userId);
        boolean save = this.save(team);
        if (!save) {
            throw new ResultException(ErrorCode.SYSTEM_ERROR, "插入team表失败");
        }

        //映射表里面插入
        Long teamId = team.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        boolean result = userTeamService.save(userTeam);
        if (!result) {
            throw new ResultException(ErrorCode.SYSTEM_ERROR, "插入user_team表失败");
        }
        return teamId;
    }
}




