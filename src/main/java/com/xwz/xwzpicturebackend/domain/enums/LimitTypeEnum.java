package com.xwz.xwzpicturebackend.domain.enums;

/**
 * 限流类型
 *
 * @author yuelu
 * @since 2024/4/15 14:40
 */
public enum LimitTypeEnum {
    /*
        userId:classSimpleName-methodName
     */
    USER_ID,
    /*
        userName:classSimpleName-methodName
    */
    @Deprecated
    USER_NAME,
    /*
        ip:classSimpleName-methodName
     */
    IP,
    /*
        :classSimpleName-methodName
     */
    GLOBAL
}
