package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class SkuSaleAttrValueDaoTest {

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void getSaleAttrsBySpuId() {
        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(2L);
        System.out.println(saleAttrsBySpuId);
    }
}