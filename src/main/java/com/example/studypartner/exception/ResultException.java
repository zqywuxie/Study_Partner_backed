package com.example.studypartner.exception;

import com.example.studypartner.common.ErrorCode;

/**
 * 2022/10/15
 *
 * @version 1.0
 * @Author:zqy
 */

/**
 * 封装全局异
 */
public class ResultException extends RuntimeException{
    private final int code;
    private final String description;


    public ResultException(int code, String description) {
        this.code = code;
        this.description = description;
    }
    public ResultException(int code,String message, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }
    public ResultException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }
    public ResultException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }
    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
