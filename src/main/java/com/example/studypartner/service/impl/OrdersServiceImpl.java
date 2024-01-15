package com.example.studypartner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.studypartner.domain.entity.Goods;
import com.example.studypartner.domain.entity.Orders;
import com.example.studypartner.domain.request.OrderAddRequest;
import com.example.studypartner.mapper.OrdersMapper;
import com.example.studypartner.service.GoodsService;
import com.example.studypartner.service.OrdersService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wuxie
 * @description 针对表【orders(订单表)】的数据库操作Service实现
 * @createDate 2024-01-15 16:49:15
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
		implements OrdersService {

	@Resource
	private GoodsService goodsService;

	/**
	 * 创建订单
	 * @param orderAddRequest 订单信息
	 * @return 订单id
	 */
	@Override
	public Long create(OrderAddRequest orderAddRequest) {
		//先查找订单表中是否有该订单
		Orders orders = this.lambdaQuery()
				.eq(Orders::getUid, orderAddRequest.getUserId())
				.eq(Orders::getGoodsId, orderAddRequest.getGoodsId())
				.last("limit 1")
				.one();

		//如果有该订单，返回订单id
		if (orders != null) {
			return orders.getOrderId();
		}
		//查找商品表中是否有该商品
		Goods goods = goodsService.getById(orderAddRequest.getGoodsId());
		if (goods == null) {
			return null;
		}
		//如果有该商品，创建订单，返回订单id
		Orders order = new Orders();
		order.setUid(orderAddRequest.getUserId());
		order.setGoodsId(orderAddRequest.getGoodsId());
		order.setPrice(goods.getPrice());
		//使用时间戳和随机数生成订单id
		order.setOrderId(System.currentTimeMillis() + (long) (Math.random() * 1000000));
		this.save(order);

		return order.getOrderId();
	}

	/**
	 * 根据订单id和我的id查询订单
	 * @param orderId 订单id
	 * @param uid 我的id
	 * @return 订单
	 */
	@Override
	public Orders getOrder(Long orderId, Long uid) {
		//先查找订单表中是否有该订单
		Orders orders = this.lambdaQuery()
				.eq(Orders::getOrderId, orderId)
				.eq(Orders::getUid, uid)
				.last("limit 1")
				.one();
		//如果有该订单，返回订单
		if (orders == null) {
			return null;
		}
		return orders;
	}

	/**
	 * 	根据我的id查询所有订单
	 * @param uid 我的id
	 * @return 订单列表
	 */
	@Override
	public List<Orders> getByUid(Long uid) {
		//根据我的id查询所有订单
		return this.lambdaQuery()
				.eq(Orders::getUid, uid)
				.list();
	}

	/**
	 * 改变订单状态
	 * @param orderId 订单id
	 * @param status 订单状态
	 */
	@Override
	public void changeStatus(Long orderId, Integer status) {
		//查询订单是否存在
		Orders orders = this.lambdaQuery()
				.eq(Orders::getOrderId, orderId)
				.last("limit 1")
				.one();
		//如果不存在，返回
		if (orders == null) {
			return;
		}
		//如果存在，如果订单状态与传入的订单状态相同，返回
		if (orders.getStatus().equals(status)) {
			return;
		}

		//更新订单状态
		orders.setStatus(status);
		this.updateById(orders);
	}
}




