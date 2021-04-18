package com.atguigu.gulimall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "gulimall.thread")
public class ThreadPoolConfigProperties {

    private Integer coreSize = 5;
    private Integer maxSize = 30;
    private Integer keepAliveTime = 10;
}
