package com.xwz.xwzpicturebackend.domain.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 度星希
 * @createTime 2025/3/23 21:31
 * @description 用户管理-用户新增-交互实体
 */

@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
