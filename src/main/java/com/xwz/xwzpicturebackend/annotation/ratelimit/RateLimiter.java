package com.xwz.xwzpicturebackend.annotation.ratelimit;


import com.xwz.xwzpicturebackend.domain.enums.LimitTypeEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流类型
 * @author yuelu
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RateLimiter {
    /**
     * 限流类型 ( 默认全局 )
     */
    LimitTypeEnum limitTypeEnum() default LimitTypeEnum.GLOBAL;

    /**
     * 对应限流规则
     */
    RateRule[] rateRules();

}
