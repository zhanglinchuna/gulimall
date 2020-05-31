package com.atguigu.gulimall.product.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SpuInfoDaoTest {

    @Autowired(required = false)
    private SpuInfoDao spuInfoDao;

    @Test
    public void updateSpuStatusTest() {
        spuInfoDao.updateSpuStatus(1L, 1);
    }
}