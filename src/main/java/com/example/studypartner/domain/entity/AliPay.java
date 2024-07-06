package com.example.studypartner.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wuxie
 * @date 2024/1/14 19:32
 * @description 该文件的描述 todo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliPay {
	private String orderId; // 商家自定义的订单编号，唯一
	private String price; // 商品价格
	private String subject; // 支付主题
}