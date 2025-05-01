package com.xwz.xwzpicturebackend.aop;



import com.xwz.xwzpicturebackend.annotation.RateLimit;
import com.xwz.xwzpicturebackend.exception.BusinessException;
import com.xwz.xwzpicturebackend.exception.ErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author 度星希
 * @createTime 2025/4/24 22:33
 * @description TODO
 */
@Aspect
@Component
public class RateLimiterAspect {
    private final Map<String, LeakyBucket> bucketCache = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String methodName = joinPoint.getSignature().toLongString();
        LeakyBucket bucket = bucketCache.computeIfAbsent(methodName,
                k -> new LeakyBucket(rateLimit.capacity(), rateLimit.rate(), rateLimit.timeUnit()));

        if (bucket.tryAcquire()) {
            return joinPoint.proceed();
        } else {
            throw new BusinessException(ErrorCode.RATE_LIMIT_ERROR, "当前接口已达流量上限");
        }
    }
}
