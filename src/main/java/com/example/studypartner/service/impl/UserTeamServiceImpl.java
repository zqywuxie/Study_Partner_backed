package com.example.studypartner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.studypartner.domain.entity.UserTeam;
import com.example.studypartner.service.UserTeamService;
import com.example.studypartner.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author wuxie
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-02-03 11:45:44
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




