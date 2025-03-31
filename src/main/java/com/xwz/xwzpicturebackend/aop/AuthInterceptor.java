package com.xwz.xwzpicturebackend.aop;

import com.xwz.xwzpicturebackend.annotation.AuthCheck;
import com.xwz.xwzpicturebackend.domain.entity.User;
import com.xwz.xwzpicturebackend.domain.enums.UserRoleEnum;
import com.xwz.xwzpicturebackend.exception.BusinessException;
import com.xwz.xwzpicturebackend.exception.ErrorCode;
import com.xwz.xwzpicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author 度星希
 * @createTime 2025/3/23 20:33
 * @description 权限校验切面
 */

@Component
@Aspect
public class AuthInterceptor {

    // 先注入UserService
    @Resource
    private UserService userService;


    /**
     *
     * @param joinPoint 连接点，就是被拦截（要增强）的方法
     * @param authCheck 注解类，可以获取属性
     * @Around("@annotation(authCheck)") 表示拦截带有AuthCheck注解的方法
     * @return
     * @throws Throwable
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 获取注解中的必须角色
        String mustRole = authCheck.mustRole();
        // 获取当前登录用户
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        // 判断注解中的角色是否在枚举中，是那就直接跳过
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 以下为：必须有该权限才通过
        // 获取当前用户具有的权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 没有权限，拒绝
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 要求必须有管理员权限，但用户没有管理员权限，拒绝
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }

}
