package com.xwz.xwzpicturebackend.domain.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 度星希
 * @createTime 2025/3/23 21:32
 * @description 用户管理-用户修改-交互实体
 */

@Data
public class UserUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
