package com.example.studypartner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.studypartner.domain.entity.UserLocation;
import com.example.studypartner.domain.vo.UserLocationVO;

import java.util.List;

/**
 * @author wuxie
 * @description 针对表【user_location(用户位置表)】的数据库操作Service
 * @createDate 2024-01-14 10:53:55
 */
public interface UserLocationService extends IService<UserLocation> {


	/**
	 * 用户当前位置
	 *
	 * @return
	 */

	UserLocation currentLocate();

	/**
	 * 附近好友
	 *
	 * @return
	 */
	List<UserLocationVO> nearbyPartners(Long userId, Integer current, Double x, Double y);


	/**
	 * 附近好友
	 *
	 * @return
	 */
	Boolean loadData();

}
