package com.example.studypartner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.studypartner.domain.Team;
import com.example.studypartner.domain.User;

/**
* @author wuxie
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-02-03 11:45:44
*/
public interface TeamService extends IService<Team> {

    long addTeam(Team team, User loginUser);
}
