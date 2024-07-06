package com.example.studypartner.mapper;

import com.example.studypartner.domain.entity.UserTeam;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wuxie
* @description 针对表【user_team(用户队伍关系)】的数据库操作Mapper
* @createDate 2023-02-03 11:45:44
* @Entity com.example.studypartner.domain.entity.UserTeam
*/
@Mapper
public interface UserTeamMapper extends BaseMapper<UserTeam> {

}




