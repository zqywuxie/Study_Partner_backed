package com.example.studypartner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.studypartner.domain.entity.FriendApplication;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wuxie
* @description 针对表【friends(好友申请管理表)】的数据库操作Mapper
* @createDate 2023-11-18 14:10:45
* @Entity com.wuxie.model.domain.Friends
*/
@Mapper
public interface FriendApplicationMapper extends BaseMapper<FriendApplication> {

}




