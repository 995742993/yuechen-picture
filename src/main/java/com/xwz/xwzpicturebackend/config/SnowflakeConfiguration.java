package com.xwz.xwzpicturebackend.config;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yuelu
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "snowflake")
public class SnowflakeConfiguration {

    /**
     * 数据中心ID
     */
    private long datacenterId;

    /**
     * 机器号 Id
     */
    private long machineId;

    @Bean
    public Snowflake snowflake() {
        // 使用配置文件中的数据中心ID和机器ID
        return IdUtil.getSnowflake(datacenterId, machineId);
    }
}