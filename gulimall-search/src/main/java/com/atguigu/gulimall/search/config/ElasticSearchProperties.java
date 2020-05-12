package com.atguigu.gulimall.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticSearchProperties {

    private String hostName;
    private Integer port;
    private String scheme;
}
