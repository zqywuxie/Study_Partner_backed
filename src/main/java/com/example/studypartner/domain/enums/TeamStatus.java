package com.example.studypartner.domain.enums;

import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.exception.ResultException;

/**
 * @author wuxie
 * 队伍状态值
 */
public enum TeamStatus {
    PUBLIC(0, "公开"),
    PRIVATE(1, "加密"),
    BANDED(2, "封禁");

    private int value;
    private String text;

    public static TeamStatus getTeamStatus(Integer value) {
        if (value == null) {
            throw new ResultException(ErrorCode.PARAMS_ERROR);
        }
        TeamStatus[] values = TeamStatus.values();
        for (TeamStatus teamStatus : values) {
            if (teamStatus.getValue() == value) {
                return teamStatus;
            }
        }
        return null;
    }

    TeamStatus(int value, String text) {
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
