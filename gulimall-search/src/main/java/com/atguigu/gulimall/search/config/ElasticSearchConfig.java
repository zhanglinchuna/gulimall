package com.atguigu.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ElasticSearchProperties.class)
public class ElasticSearchConfig {

    @Bean
    public RestHighLevelClient esRestClient(ElasticSearchProperties properties) {

        RestClientBuilder builder = RestClient.builder(new HttpHost(properties.getHostName(), properties.getPort(), properties.getScheme()));
        return new RestHighLevelClient(builder);
    }
}
