package com.example.studypartner.service;

import com.example.studypartner.StudyPartnerApplication;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.entity.UserLocation;
import com.example.studypartner.domain.vo.UserLocationVO;
import com.example.studypartner.service.UserLocationService;
import com.example.studypartner.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wuxie
 * @date 2024/1/14 13:26
 * @description 该文件的描述 todo
 */


@SpringBootTest
public class loadDataGEO {

	@Resource
	private UserService userService;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private UserLocationService userLocationService;
	@Test
	public void loadShopData() {
		// 1.查询所有店铺信息
		List<UserLocation> list = userLocationService.list();
		// 2.把店铺分组，按照typeId分组，typeId一致的放到一个集合
		Map<Long, List<UserLocation>> map = list.stream().collect(Collectors.groupingBy(UserLocation::getUserId));
		// 3.分批完成写入Redis
		for (Map.Entry<Long, List<UserLocation>> entry : map.entrySet()) {
			// 3.1.获取类型id
			Long typeId = entry.getKey();
			String key = "shop:geo:" + typeId;
			// 3.2.获取同类型的店铺的集合
			List<UserLocation> value = entry.getValue();
			List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(value.size());
			// 3.3.写入redis GEOADD key 经度 纬度 member
			for (UserLocation userLocation : value) {
				// 设置店铺id，x,y坐标
				//  GEOADD key longitude latitude member [longitude latitude member ...]

				// 3.3.1 一条一条添加写入，效率较低
				// stringRedisTemplate.opsForGeo().add(key, new Point(shop.getX(), shop.getY()), shop.getId().toString());

				// 3.3.2 批量添加再一次性写入
				locations.add(new RedisGeoCommands.GeoLocation<>(
						userLocation.getId().toString(),
						new Point(userLocation.getLongitude(), userLocation.getLatitude())
				));
			}
			stringRedisTemplate.opsForGeo().add(key, locations);
		}
	}


	@Test
	public void test() {
		System.out.println(userLocationService.list());
	}

}
