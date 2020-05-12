package com.atguigu.gulimall.search;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;
    @Test
    void contextLoads() {
        System.out.println(applicationContext.getBean("esRestClient"));
    }

}
