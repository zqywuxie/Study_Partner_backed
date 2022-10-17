package com.example.usercenterback.common;

import lombok.Data;

/**
 * 2022/10/15
 *
 * @version 1.0
 * @Author:zqy
 */
@Data
public class CommonResult<T> {
    private int code;
    private String message;
    private T data;
    private String description;

    /**
     * 成功封装的类
     * @param code
     * @param message
     * @param data
     * @param description
     */

    public CommonResult(int code, String message, T data, String description) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.description = description;
    }


    public CommonResult(int code, T data, String message) {
        this(code, message,data,"");
    }

    /**
     * 失败封装的类 数据为null
     * @param errorCode
     */
    public CommonResult(ErrorCode errorCode)
    {
        this(errorCode.getCode(),errorCode.getMessage(),null,errorCode.getDescription());
    }

}
