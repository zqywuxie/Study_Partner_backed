package com.example.studypartner.controller;

import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.domain.entity.Orders;
import com.example.studypartner.domain.entity.User;
import com.example.studypartner.domain.request.OrderAddRequest;
import com.example.studypartner.service.OrdersService;
import com.example.studypartner.service.UserService;
import com.example.studypartner.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author wuxie
 * @date 2024/1/15 16:43
 * @description 该文件的描述 todo
 */

//订单接口
@RequestMapping("/order")
@RestController
@Slf4j
public class OrderController {

	@Resource
	private OrdersService orderService;

	@Resource
	private UserService userService;

	//post请求，requestbody得到，接受商品id和用户id，创建订单接口
	@PostMapping("/create")
	public CommonResult<Long> create(@RequestBody OrderAddRequest orderAddRequest) {
		Long orderId = orderService.create(orderAddRequest);
		return ResultUtils.success(orderId);
	}

	//根据id获得订单信息接口
	@GetMapping("/getById")
	public CommonResult<Orders> getById(@RequestParam Long orderId, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		if (loginUser == null) {
			return ResultUtils.failed(ErrorCode.NOT_LOGIN, "用户未登录");
		}
		Orders orders = orderService.getOrder(orderId, loginUser.getId());
		return ResultUtils.success(orders);
	}

}
