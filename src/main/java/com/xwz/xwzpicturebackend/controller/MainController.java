package com.xwz.xwzpicturebackend.controller;

import com.xwz.xwzpicturebackend.annotation.ratelimit.RateLimiter;
import com.xwz.xwzpicturebackend.annotation.ratelimit.RateLimiters;
import com.xwz.xwzpicturebackend.annotation.ratelimit.RateRule;
import com.xwz.xwzpicturebackend.common.BaseResponse;
import com.xwz.xwzpicturebackend.common.ResultUtils;
import com.xwz.xwzpicturebackend.domain.enums.LimitTypeEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author 度星希
 * @createTime 2025/3/23 13:57
 * @description TODO
 */

@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检查
     */
    @GetMapping("/health")
    //  @RateLimit(capacity = 5, rate = 1, timeUnit = TimeUnit.SECONDS)
    @RateLimiters(rateLimiters = {

             @RateLimiter(limitTypeEnum = LimitTypeEnum.IP, rateRules = {
                     @RateRule(limit = 1000, timeDuration = 1, timeUnit = TimeUnit.DAYS),
                     @RateRule(limit = 10, timeDuration = 1, timeUnit = TimeUnit.MINUTES)
             }
             ),
            @RateLimiter(limitTypeEnum = LimitTypeEnum.USER_ID, rateRules = {
                    @RateRule(limit = 500, timeDuration = 1, timeUnit = TimeUnit.DAYS),
                    @RateRule(limit = 5, timeDuration = 1, timeUnit = TimeUnit.MINUTES)
            }
            )

    }
    )
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}

