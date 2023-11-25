package com.example.studypartner.mapper;

import com.example.studypartner.domain.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 思无邪
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2022-10-10 16:49:45
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




