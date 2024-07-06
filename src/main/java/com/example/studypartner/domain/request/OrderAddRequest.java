package com.example.studypartner.domain.request;

import lombok.Data;

/**
 * @author wuxie
 * @date 2024/1/15 16:56
 * @description 该文件的描述 todo
 */

//订单添加请求
@Data
public class OrderAddRequest {
	//商品id
	private Long goodsId;
	//用户id
	private Long userId;
}
