package com.xwz.xwzpicturebackend.domain.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 度星希
 * @createTime 2025/3/23 17:39
 * @description TODO
 */

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3021429350469464931L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 验证码
     */
    private String captcha;

}

