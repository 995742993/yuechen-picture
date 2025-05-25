package com.xwz.xwzpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xwz.xwzpicturebackend.domain.dto.user.UserQueryRequest;
import com.xwz.xwzpicturebackend.domain.entity.User;
import com.xwz.xwzpicturebackend.domain.vo.user.LoginUserVO;
import com.xwz.xwzpicturebackend.domain.vo.user.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author yuelu
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-03-23 17:24:21
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取用户脱敏后的实体
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    String getEncryptPassword(String userPassword);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    String sendEmailCode(String userEmail);

    String secureSendCode(String userEmail);

    long userRegisterByEmail(String userAccount, String userPassword, String checkPassword, String captcha);
}
