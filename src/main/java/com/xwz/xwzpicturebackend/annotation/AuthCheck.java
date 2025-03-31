package com.xwz.xwzpicturebackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 度星希
 * @createTime 2025/3/23 20:31
 * @description TODO
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {
    /**
     * 必须的角色
     * @return
     */
    String mustRole() default "";
}
