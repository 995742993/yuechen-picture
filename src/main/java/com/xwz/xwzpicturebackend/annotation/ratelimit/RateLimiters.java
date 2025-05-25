package com.xwz.xwzpicturebackend.annotation.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流组
 *
 * @author: yuelu
 * @since: 2022/8/10 17:36
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RateLimiters {
    /**
     * 多个限流规则
     */
    RateLimiter[] rateLimiters();
}

