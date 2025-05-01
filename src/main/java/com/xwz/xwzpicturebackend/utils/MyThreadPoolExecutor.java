package com.xwz.xwzpicturebackend.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 度星希
 * @createTime 2025/4/23 22:18
 * @description TODO
 */
@Slf4j
public class MyThreadPoolExecutor {

    // 更健壮的实现
    public ThreadPoolExecutor createThreadFirstPool() {
        int core = 10;
        int max = 50;
        int queueSize = 100;

        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueSize) {
            @Override
            // 将元素插入队列
            public boolean offer(Runnable e) {
                // 先尝试返回false让线程池扩容
                return false;
            }
        };

        return new ThreadPoolExecutor(
                core,
                max,
                60,
                TimeUnit.SECONDS,
                queue,
                new ThreadFactory() {
                    private final AtomicInteger count = new AtomicInteger(1);
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "tpool-" + count.getAndIncrement());
                    }
                },
                (r, executor) -> {
                    // 拒绝时的处理：最后一次尝试入队
                    if (!executor.getQueue().offer(r)) {
                        throw new RejectedExecutionException(
                                "线程池已满(" + executor.getPoolSize() + "/" + max + "), " +
                                        "队列已满(" + executor.getQueue().size() + "/" + queueSize + ")");
                    }
                }
        );
    }

}
