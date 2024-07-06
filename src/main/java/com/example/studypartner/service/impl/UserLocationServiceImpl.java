package com.example.studypartner.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.entity.UserLocation;
import com.example.studypartner.domain.vo.UserLocationVO;
import com.example.studypartner.mapper.UserLocationMapper;
import com.example.studypartner.service.UserLocationService;
import com.example.studypartner.service.UserService;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.studypartner.constant.SystemConstant.DEFAULT_PAGE_SIZE;

/**
 * @author wuxie
 * @description 针对表【user_location(用户位置表)】的数据库操作Service实现
 * @createDate 2024-01-14 10:53:55
 */
@Service
public class UserLocationServiceImpl extends ServiceImpl<UserLocationMapper, UserLocation>
		implements UserLocationService {


	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private UserService userService;

	@Resource
	private UserLocationService userLocationService;

	@Override
	public UserLocation currentLocate() {
		return null;
	}

	@Override
	public List<UserLocationVO> nearbyPartners(Long userId, Integer current, Double x, Double y) {
		// 2.计算分页参数
		long from = (current - 1) * DEFAULT_PAGE_SIZE;
		long end = current * DEFAULT_PAGE_SIZE;

		if (x == null && y == null) {
			LambdaQueryWrapper<UserLocation> userLocationLambdaQueryWrapper = new LambdaQueryWrapper<>();
			userLocationLambdaQueryWrapper.eq(UserLocation::getUserId, userId);
			UserLocation one = userLocationService.getOne(userLocationLambdaQueryWrapper);
			x = one.getLongitude();
			y = one.getLatitude();
		}
		// 3.查询redis、按照距离排序、分页。结果：shopId、distance
		String key = "shop:geo:user:";
		Circle circle = new Circle(new Point(x, y), new Distance(30, Metrics.KILOMETERS));
		GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
				// 原始命令： GEOSEARCH key BYLONLAT x y BYRADIUS 10 WITHDISTANCE
				.radius(
						key, circle
				);
		// 4.解析出id
		if (results == null) {
			return null;
		}
		List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
		if (list.size() <= from) {
			// 没有下一页了，结束
			return null;
		}
		// 4.1.由于geosearch始终是0~end,因此需要分页效果需要直接手动截取
		// 手动截取 from ~ end的部分
		List<Long> ids = new ArrayList<>(list.size());
		Map<String, Distance> distanceMap = new HashMap<>(list.size());
		list.stream().skip(from).forEach(result -> {
			// 4.2.获取店铺id
			String shopIdStr = result.getContent().getName();
			ids.add(Long.valueOf(shopIdStr));
			// 4.3.获取距离
			Distance distance = result.getDistance();
			distanceMap.put(shopIdStr, distance);
		});
		// 5.根据id查询Shop
		String idStr = StrUtil.join(",", ids);

		LambdaQueryWrapper<UserLocation> userLocationLambdaQueryWrapper = new LambdaQueryWrapper<>();
		userLocationLambdaQueryWrapper.in(UserLocation::getId, idStr).select(UserLocation::getUserId);

		LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();

		userLambdaQueryWrapper.in(User::getId, ids).last("ORDER BY FIELD(id," + idStr + ")");

		List<User> users = userService.list(userLambdaQueryWrapper).stream().map(user -> userService.cleanUser(user)).collect(Collectors.toList());
		List<UserLocationVO> userLocationVOS = new ArrayList<>(users.size());
		for (User user : users) {
			UserLocationVO userLocationVO = new UserLocationVO();
			userLocationVO.setUser(user);
			userLocationVO.setDistance(distanceMap.get(user.getId().toString()).getValue());
			userLocationVOS.add(userLocationVO);
		}
		// 6.返回
		return userLocationVOS;
	}

	@Override
	public Boolean loadData() {
		// 1.查询所有店铺信息
		List<UserLocation> list = this.list();
		// 2.把店铺分组，按照typeId分组，typeId一致的放到一个集合
		Map<Long, List<UserLocation>> map = list.stream().collect(Collectors.groupingBy(UserLocation::getUserId));
		// 3.分批完成写入Redis
		for (Map.Entry<Long, List<UserLocation>> entry : map.entrySet()) {
			// 3.1.获取类型id
			Long typeId = entry.getKey();
			String key = "shop:geo:user:";
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
						userLocation.getUserId().toString(),
						new Point(userLocation.getLongitude(), userLocation.getLatitude())
				));
			}
			stringRedisTemplate.opsForGeo().add(key, locations);
		}
		return true;
	}

}




