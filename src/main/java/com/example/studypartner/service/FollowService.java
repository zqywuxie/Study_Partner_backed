package com.example.studypartner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.studypartner.domain.entity.Follow;
import com.example.studypartner.domain.vo.UserVO;

import java.util.List;

/**
 * @author wuxie
 * @description 针对表【follow】的数据库操作Service
 * @createDate 2023-11-20 10:02:50
 */
public interface FollowService extends IService<Follow> {

	/**
	 * 关注
	 *
	 * @param id
	 * @param followerId
	 * @return
	 */

	void followUser(Long loginUserId, Long followerId);


	/**
	 * 获得我的粉丝
	 *
	 * @param loginUser
	 * @return
	 */
	List<UserVO> listFans(Long loginUser);


	/**
	 * 获得我的关注
	 *
	 * @param loginUser
	 * @return
	 */
	List<UserVO> listMyFollow(Long loginUser);

	Integer fansCount(Long loginUser);
	Integer myFollowCount(Long loginUser);
}
