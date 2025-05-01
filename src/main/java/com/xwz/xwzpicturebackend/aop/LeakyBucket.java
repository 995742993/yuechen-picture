package com.xwz.xwzpicturebackend.aop;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 度星希
 * @createTime 2025/4/24 22:31
 * @description 漏桶算法实现限流
 */

public class LeakyBucket {

    private final long capacity;
    private final long leakIntervalNanos; // 纳秒级间隔
    private final AtomicLong lastLeakTime;
    private final AtomicLong water;

    public LeakyBucket(long capacity, long rate, TimeUnit timeUnit) {
        this.capacity = capacity;
        this.leakIntervalNanos = timeUnit.toNanos(1) / rate;
        this.lastLeakTime = new AtomicLong(System.nanoTime());
        this.water = new AtomicLong(0);
    }

    public boolean tryAcquire() {
        long now = System.nanoTime();
        long lastTime = lastLeakTime.get();
        long elapsed = now - lastTime;

        // 计算漏水量
        long leaked = elapsed / leakIntervalNanos;
        if (leaked > 0) {
            // CAS更新时间和水位
            if (lastLeakTime.compareAndSet(lastTime, now)) {
                water.updateAndGet(current -> Math.max(0, current - leaked));
            }
        }

        // 尝试获取
        long currentWater;
        do {
            currentWater = water.get();
            if (currentWater >= capacity) {
                return false;
            }
        } while (!water.compareAndSet(currentWater, currentWater + 1));

        return true;
    }
}
