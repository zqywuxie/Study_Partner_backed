package com.example.studypartner.job;

import cn.hutool.core.date.StopWatch;
import com.example.studypartner.domain.entity.Orders;
import com.example.studypartner.domain.enums.OrderStatus;
import com.example.studypartner.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author wuxie
 * @date 2024/1/15 18:53
 * @description 删除取消订单
 */
@Slf4j
public class DeleteCancelOrders extends QuartzJobBean {

	@Resource
	private OrdersService ordersService;

	@Resource
	private RedissonClient redissonClient;

	StopWatch stopWatch = new StopWatch();

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		//通过redisson锁删除取消订单
		//获得锁
		RLock lock = redissonClient.getLock("deleteCancelOrders");
		try {
			//-1 默认值表示不会过期
			if (lock.tryLock(0, -1, TimeUnit.SECONDS)) {
				System.out.println("开始删除已经取消的订单");
				stopWatch.start();
				List<Orders> list = ordersService.list();
				for (Orders order : list) {
					if (order.getStatus().equals(OrderStatus.DELIVERED.getCode())) {
						ordersService.removeById(order.getId());
					}
				}
				stopWatch.stop();
				log.info("删除已取消订单结束，花费时间为:{}", stopWatch.getTotalTimeSeconds());
			}
		} catch (InterruptedException e) {
			log.error("deleteCancelOrders is error", e);
		} finally {
			if (lock.isHeldByCurrentThread()) {
				System.out.println("unlock:" + Thread.currentThread().getId());
				lock.unlock();
			}
		}
	}
}
