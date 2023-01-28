package com.example.studypartner.exception;

import com.example.studypartner.common.CommonResult;
import com.example.studypartner.common.ErrorCode;
import com.example.studypartner.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局捕获器
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResultException.class)
    public CommonResult businessExceptionHandler(ResultException e) {
        log.error("businessException: " + e.getMessage(), e);
        return ResultUtils.failed(e.getCode(), e.getMessage(), e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public CommonResult runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeException", e);
        return ResultUtils.failed(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
    }
}
