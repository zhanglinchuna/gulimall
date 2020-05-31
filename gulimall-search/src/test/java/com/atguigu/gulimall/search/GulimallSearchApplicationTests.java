package com.atguigu.gulimall.search;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Date;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Qualifier("esRestClient")
    @Autowired
    private RestHighLevelClient client;

    @Test
    void contextLoads() {
        System.out.println(applicationContext.getBean("esRestClient"));
    }

    @Test
    public void indexTest() throws IOException {
        IndexRequest posts = new IndexRequest("posts")
                .id("1")
                .source("user", "kimchy",
                        "postDate", new Date(),
                        "message", "trying out Elasticsearch");

        IndexResponse index = client.index(posts, RequestOptions.DEFAULT);
    }
}
