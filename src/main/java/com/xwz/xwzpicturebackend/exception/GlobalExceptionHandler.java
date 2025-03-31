package com.xwz.xwzpicturebackend.exception;

import com.xwz.xwzpicturebackend.common.BaseResponse;
import com.xwz.xwzpicturebackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author 度星希
 * @createTime 2025/3/23 13:51
 * @description 通用全局异常处理
 * tips：
 * 1. @RestControllerAdvice这个注解表明这个类是一个全局异常处理类，它将处理整个应用程序中的异常
 * 2. @ExceptionHandler(BusinessException.class)当应用程序抛出 BusinessException 或其子类（如果有的话）时，
 *      Spring 会自动调用被 @ExceptionHandler 注解标记的方法来处理这个异常。
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}


