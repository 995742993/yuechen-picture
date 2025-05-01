package com.xwz.xwzpicturebackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author 度星希
 * @createTime 2025/4/24 22:32
 * @description TODO
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RateLimit {
    int capacity() default 10;           // 桶的容量
    int rate() default 1;                // 速率
    TimeUnit timeUnit() default TimeUnit.SECONDS; // 时间单位
}