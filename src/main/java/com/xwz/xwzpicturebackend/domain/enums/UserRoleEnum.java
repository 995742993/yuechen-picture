package com.xwz.xwzpicturebackend.domain.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 度星希
 * @createTime 2025/3/23 17:32
 * @description 用户角色枚举类
 */

@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;

    private final String value;

    private static final Map<String, UserRoleEnum> userRoleMap;

    static {
        userRoleMap = new HashMap<>();
        userRoleMap.put(USER.getValue(), USER);
        userRoleMap.put(ADMIN.getValue(), ADMIN);
    }

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        return userRoleMap.get(value);
    }
}

