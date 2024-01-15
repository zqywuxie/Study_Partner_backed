package com.example.studypartner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.studypartner.domain.entity.Orders;
import com.example.studypartner.domain.request.OrderAddRequest;

import java.util.List;

/**
 * @author wuxie
 * @description 针对表【orders(订单表)】的数据库操作Service
 * @createDate 2024-01-15 16:49:15
 */
public interface OrdersService extends IService<Orders> {

	/**
	 * 创建订单
	 *
	 * @param orderAddRequest 订单信息
	 * @return 订单id
	 */
	Long create(OrderAddRequest orderAddRequest);


	/**
	 * 根据订单id和用户id查询订单
	 * @param orderId 订单id
	 * @param uid 用户id
	 * @return 订单
	 */
	Orders getOrder(Long orderId, Long uid);

	/**
	 * 根据用户id查询订单
	 * @param uid 用户id
	 * @return 订单列表
	 */
	List<Orders> getByUid(Long uid);

	/**
	 * 根据订单id修改订单状态
	 * @param orderId 订单id
	 * @param status 订单状态
	 */
	void changeStatus(Long orderId, Integer status);
}
