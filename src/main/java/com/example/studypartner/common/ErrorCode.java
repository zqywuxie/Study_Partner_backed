package com.example.studypartner.common;

import lombok.Getter;

/**
 * 2022/10/15
 *
 * @version 1.0
 * @Author:zqy
 */


@Getter
public enum ErrorCode {
	SUCCESS(0, "success", ""),
	PARAMS_ERROR(40000, "请求参数错误", ""),
	NULL_ERROR(40001, "请求数据为空", ""),
	NOT_LOGIN(40002, "未登录", ""),
	NOT_ADMIN(40003, "非管理员", ""),
	REPEAT_ERROR(40004, "数据重复", ""),
	INSERT_ERROR(40005, "添加失败", ""),
	NO_AUTH(40006, "无权限", ""),
	TOO_MANY_REQUEST(42900, "请求过于频繁", ""),
	SYSTEM_ERROR(50000, "系统故障", ""),
	OPERATION_ERROR(50001, "操作失败", "");

	private final int code;
	private final String message;
	private final String description;

	ErrorCode(int code, String message, String description) {
		this.code = code;
		this.message = message;
		this.description = description;
	}
}
