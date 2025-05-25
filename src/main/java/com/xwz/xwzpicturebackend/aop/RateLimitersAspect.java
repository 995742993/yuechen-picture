package com.xwz.xwzpicturebackend.aop;

import cn.hutool.core.lang.Snowflake;
import com.xwz.xwzpicturebackend.annotation.ratelimit.RateLimiter;
import com.xwz.xwzpicturebackend.annotation.ratelimit.RateLimiters;
import com.xwz.xwzpicturebackend.annotation.ratelimit.RateRule;
import com.xwz.xwzpicturebackend.domain.entity.User;
import com.xwz.xwzpicturebackend.exception.BusinessException;
import com.xwz.xwzpicturebackend.service.UserService;
import com.xwz.xwzpicturebackend.utils.IpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import static com.xwz.xwzpicturebackend.exception.ErrorCode.NOT_LOGIN_ERROR;
import static com.xwz.xwzpicturebackend.exception.ErrorCode.OPERATION_ERROR;

/**
 * Redis 多规则限流
 *
 * @author yuelu
 * @since 2024/5/10 20:43
 */
@Aspect
@Order(20)
@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitersAspect {
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    @Qualifier("rateLimiterScript")
    private final RedisScript<Boolean> limitScript;

    @Resource
    private Snowflake snowflake;

    @Resource
    private UserService userService;




    @Before(value = "@annotation(rateLimiters)")
    public void boBefore(JoinPoint joinPoint, RateLimiters rateLimiters) {

        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) Objects
                .requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        User user = userService.getLoginUser(httpServletRequest);
//        if (userService.isAdmin(user)) {
//            return;
//        }
        RateLimiter[] limiters = rateLimiters.rateLimiters();
        for (RateLimiter limiter : limiters) {
            // 1. 生成限流key
            String limitKey = generateLimiterKey(joinPoint, limiter, httpServletRequest,  user);
            // 2. 执行脚本返回是否限流成功 (传入key，唯一标识,当前时间 )
            Boolean execute = redisTemplate.execute(
                    limitScript,
                    List.of(limitKey, snowflake.nextIdStr(), String.valueOf(System.currentTimeMillis())),
                    getRules(limiter));
            // 3. 判断是否限流
            if (Boolean.TRUE.equals(execute)) {
                // 3.2 抛出限流错误
                throw new BusinessException(OPERATION_ERROR, "操作过于频繁");
            }
        }
    }

    /**
     * 生成限流key
     */
    private String generateLimiterKey(JoinPoint joinPoint, RateLimiter limiter, HttpServletRequest httpServletRequest, User user) {
        StringBuilder key = new StringBuilder("RATE:LIMIT:");
        switch (limiter.limitTypeEnum()) {
            case IP: {
                // 1. IP 模式
                key.append(IpUtil.getIpAddr(httpServletRequest)).append(":");
            }
            case USER_ID: {
                // 2. userId 模式
                Long userId = user.getId();
                if (userId != null) {
                    key.append(userId).append(":");
                } else {
                    log.error("RateLimitersAspect.generateLimiterKey() => 无法获取到Id");
                    throw new BusinessException(NOT_LOGIN_ERROR);
                }
            }
            default:{}
        }

        // 拼接 role:类名:方法名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        key.append(targetClass.getSimpleName()).append(":").append(method.getName());
        return key.toString();
    }

    /**
     * 获取规则集合
     */
    private Object[] getRules(RateLimiter limiter) {
        RateRule[] rateRules = limiter.rateRules();
        // 1. 创建返回对象 ( i * 2 === i << 1)
        Object[] result = new Object[rateRules.length * 2];
        // 2. 遍历规则返回
        for (int i = 0; i < rateRules.length; i++) {
            result[i * 2] = rateRules[i].limit();
            result[i * 2 + 1] = rateRules[i].timeUnit().toMillis(rateRules[i].timeDuration());
        }
        // 3. 返回结果
        return result;
    }


}
