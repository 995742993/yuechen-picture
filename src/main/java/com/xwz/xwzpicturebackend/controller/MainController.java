package com.xwz.xwzpicturebackend.controller;

import com.xwz.xwzpicturebackend.annotation.RateLimit;
import com.xwz.xwzpicturebackend.common.BaseResponse;
import com.xwz.xwzpicturebackend.common.ResultUtils;
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
    @RateLimit(capacity = 5, rate = 1, timeUnit = TimeUnit.SECONDS)
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}

