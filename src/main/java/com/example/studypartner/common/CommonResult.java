package com.example.studypartner.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 2022/10/15
 *
 * @version 1.0
 * @Author:zqy
 */
@ApiModel(description = "2022/10/15")
@Data
public class CommonResult<T> {
    @ApiModelProperty("响应码")
    private int code;
    @ApiModelProperty("响应信息")
    private String message;
    @ApiModelProperty("响应数据")
    private T data;
    @ApiModelProperty("响应描述")
    private String description;

    /**
     * 成功封装的类
     * @param code 响应码
     * @param message 响应信息
     * @param data 响应数据
     * @param description 响应描述
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
     * @param errorCode 失败返回码
     */
    public CommonResult(ErrorCode errorCode)
    {
        this(errorCode.getCode(),errorCode.getMessage(),null,errorCode.getDescription());
    }

}
