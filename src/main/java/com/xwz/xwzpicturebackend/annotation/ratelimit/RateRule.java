package com.xwz.xwzpicturebackend.annotation.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 限流规则
 * @author yuelu
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RateRule {

    /**
     * 限流次数
     */
    long limit() default 10;

    /**
     * 限流时间
     */
    long timeDuration() default 60;

    /**
     * 限流时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

}
