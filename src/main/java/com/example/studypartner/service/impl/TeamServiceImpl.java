package com.example.studypartner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.Team;
import com.example.studypartner.domain.User;
import com.example.studypartner.domain.UserTeam;
import com.example.studypartner.domain.dto.TeamDTO;
import com.example.studypartner.domain.enums.TeamStatus;
import com.example.studypartner.domain.request.TeamJoinInfo;
import com.example.studypartner.domain.request.TeamUpdateInfo;
import com.example.studypartner.domain.vo.TeamUserVO;
import com.example.studypartner.domain.vo.UserVO;
import com.example.studypartner.exception.ResultException;
import com.example.studypartner.mapper.TeamMapper;
import com.example.studypartner.service.TeamService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    @Resource
    private UserService userService;

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

    /**
     * todo **关联查询已加入队伍的用户信息（可能会很耗费性能，建议大家用自己写 SQL 的方式实现）**
     */
    @Override
    public List<TeamUserVO> listTeams(TeamDTO teamDTO, boolean isAdmin) {
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        if (teamDTO != null) {
            Long id = teamDTO.getId();
            if (id != null && id > 0) {
                teamQueryWrapper.eq("id", id);
            }
            String description = teamDTO.getDescription();
            if (StringUtils.isNotBlank(description)) {
                teamQueryWrapper.eq("description", description);
            }
            Integer status = teamDTO.getStatus();

            TeamStatus teamStatus = TeamStatus.getTeamStatus(status);
            if (teamStatus == null) {
                teamStatus = TeamStatus.PUBLIC;
            }
            if (!isAdmin && teamStatus != TeamStatus.PUBLIC) {
                throw new ResultException(ErrorCode.NOT_ADMIN);
            }
            teamQueryWrapper.eq("status", teamStatus.getValue());

            String name = teamDTO.getName();
            if (StringUtils.isNotBlank(name)) {
                teamQueryWrapper.eq("name", name);
            }
            Integer maxNum = teamDTO.getMaxNum();
            if (maxNum != null && maxNum <= 5) {
                teamQueryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamDTO.getUserId();
            if (userId != null && userId > 0) {
                teamQueryWrapper.eq("userId", userId);
            }
            String searchText = teamDTO.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                teamQueryWrapper.and(tq -> tq.like("name", searchText).or().like("description", searchText));
            }


        }
        //查询过期时间
        //expireTime is not null or expireTime > new Date()
        teamQueryWrapper.and(tq -> tq.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> list = this.list(teamQueryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        //关联查询 映射表
        //select * from team t
        //left join user_team ut on ut.id=t.id
        //left join user u on u.id=ut.userId
        ArrayList<TeamUserVO> teamUserVOS = new ArrayList<>();
        for (Team team : list) {
            Long createUserId = team.getUserId();
            if (createUserId == null) {
                continue;
            }
            User user = userService.getById(createUserId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOS.add(teamUserVO);

        }
        return teamUserVOS;
    }

    @Override
    public boolean updateTeam(TeamUpdateInfo teamUpdateInfo, User loginUser) {
        if (teamUpdateInfo == null) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateInfo.getId();
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
        Integer status = teamUpdateInfo.getStatus();
        String password = teamUpdateInfo.getPassword();
        TeamStatus teamStatus = TeamStatus.getTeamStatus(status);
        //todo  原本加密改为加密后的优化
        if (teamStatus == TeamStatus.SECRET) {
            if (StringUtils.isBlank(password)) {
                throw new ResultException(ErrorCode.NULL_ERROR, "加密房间必须要有密码");
            }
        }

        Team team1 = new Team();
        BeanUtils.copyProperties(teamUpdateInfo, team1);
        return this.updateById(team1);
    }

    @Override
    public boolean joinTeam(TeamJoinInfo teamJoinInfo, User loginUser) {
        if (teamJoinInfo == null) {
            throw new ResultException(ErrorCode.NULL_ERROR);
        }

        //队伍必须存在
        Long teamId = teamJoinInfo.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new ResultException(ErrorCode.NULL_ERROR, "加入队伍不存在");
        }


        //不能主动加入私人队伍
        Integer status = team.getStatus();
        TeamStatus teamStatus = TeamStatus.getTeamStatus(status);
        if (TeamStatus.PRIVATE.equals(teamStatus)) {
            throw new ResultException(ErrorCode.NULL_ERROR, "不能加入私人队伍");
        }
        //队伍状态是加密的，输入密码要比对
        String password = teamJoinInfo.getPassword();
        if (TeamStatus.SECRET.equals(teamStatus)) {
            if (password == null || !password.equals(team.getPassword())) {
                throw new ResultException(ErrorCode.NULL_ERROR, "密码不正确");
            }
        }
        //不能加入未过期的队伍
        Date expireTime = team.getExpireTime();
        if (expireTime.before(new Date())) {
            throw new ResultException(ErrorCode.NULL_ERROR, "队伍已过期");
        }

        //查询当前用户加入队伍的数量
        Long loginUserId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", loginUserId);
        long count = userTeamService.count(userTeamQueryWrapper);
        if (count >= 5) {
            throw new ResultException(ErrorCode.INSERT_ERROR, "用户加入队伍过多,最多加入5个队伍");
        }

        //判断队伍用户是否已满
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        long userCount = userTeamService.count(userTeamQueryWrapper);
        if (userCount > team.getMaxNum()) {
            throw new ResultException(ErrorCode.NULL_ERROR, "队伍已满");
        }
        //不能重复加入队伍
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        userTeamQueryWrapper.eq("userId", loginUserId);
        long hasUserJoin = userTeamService.count(userTeamQueryWrapper);
        if (hasUserJoin > 0) {
            throw new ResultException(ErrorCode.NULL_ERROR, "用户已加入队伍");
        }

        //修改队伍信息
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(loginUserId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }
}




