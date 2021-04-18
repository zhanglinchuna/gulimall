package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class AttrGroupDaoTest {

    @Autowired
    AttrGroupDao attrGroupDao;
    @Test
    public void getAttrGroupWithAttrsBySpuId() {
        List<SpuItemAttrGroupVo> vos = attrGroupDao.getAttrGroupWithAttrsBySpuId(2L, 225L);
        System.out.println(vos);
    }
}