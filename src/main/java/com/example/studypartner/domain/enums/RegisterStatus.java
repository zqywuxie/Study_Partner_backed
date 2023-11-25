package com.example.studypartner.domain.enums;

import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.exception.ResultException;

/**
 * @author wuxie
 * 队伍状态值
 */
public enum RegisterStatus {
    ACCOUNT_LEN(0, "账号长度大于4"),
	PASSWORD_LEN(1, "密码长度大于6"),
	REPEAT_ERROR(2, "账号已注册"),

	ACCOUNT_PARAMS(3,"账号不能有特殊符号"),
	PASSWORD_CHECK(4,"两次密码不一致");

    private int value;
    private String text;

    public static RegisterStatus getTeamStatus(Integer value) {
        if (value == null) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);
        }
        RegisterStatus[] values = RegisterStatus.values();
        for (RegisterStatus teamStatus : values) {
            if (teamStatus.getValue() == value) {
                return teamStatus;
            }
        }
        return null;
    }

    RegisterStatus(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
