package com.example.studypartner.domain.enums;

import lombok.Data;

/**
 * @author wuxie
 * @date 2024/1/15 18:48
 * @description 该文件的描述 todo
 */

public enum OrderStatus {
	//订单状态
	NOT_PAY(0, "未支付"),
	PAID(1, "已支付"),
	DELIVERED(2, "已取消"),
	FINISHED(3, "已完成"),
	//异常
	EXCEPTION(4, "异常");

	private Integer code;
	private String desc;

	//构造方法
	OrderStatus(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	//get方法
	public Integer getCode() {
		return code;
	}

	//get方法
	public String getDesc() {
		return desc;
	}

	//set方法
	public void setCode(Integer code) {
		this.code = code;
	}

	//set方法
	public void setDesc(String desc) {
		this.desc = desc;
	}
}
