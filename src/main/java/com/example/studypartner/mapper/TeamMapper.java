package com.example.studypartner.mapper;

import com.example.studypartner.domain.entity.Team;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wuxie
* @description 针对表【team(队伍)】的数据库操作Mapper
* @createDate 2023-02-03 11:45:44
* @Entity com.example.studypartner.domain.entity.Team
*/
@Mapper
public interface TeamMapper extends BaseMapper<Team> {

}




