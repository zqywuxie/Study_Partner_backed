package com.example.studypartner.utils;

import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;

/**
 * 2022/10/15
 *
 * @version 1.0
 * @Author:zqy
 */
public class ResultUtils {
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(0, data, "success");
    }

    public static CommonResult failed(ErrorCode errorCode) {
        return new CommonResult<>(errorCode);
    }
    public static <T> CommonResult<T> failed(int code,String message,String description) {
        return new CommonResult<>(code, message,null, description);
    }
    public static <T> CommonResult<T> failed(ErrorCode errorCode,String message,String description) {
        return new CommonResult<>(errorCode.getCode(),message,null, description);
    }
    public static <T> CommonResult<T> failed(ErrorCode errorCode,String description) {
        return new CommonResult<>(errorCode.getCode(), errorCode.getMessage(),null, description);
    }
}
