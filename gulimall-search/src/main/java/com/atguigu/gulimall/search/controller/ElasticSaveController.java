package com.atguigu.gulimall.search.controller;

import com.atguigu.common.enume.BizCodeEnume;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    private ProductSaveService productSaveService;

    @PostMapping("/product")
    public R saveProduct(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean flag = false;
        try {
            flag = productSaveService.productStatUp(skuEsModels);
        } catch (Exception e) {
            log.error("ElasticSaveController，es保存商品检索信息失败：{}", e);
            return R.error(BizCodeEnume.PORDUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PORDUCT_UP_EXCEPTION.getMsg());
        }
        if (!flag) {
            return R.ok();
        } else {
            return R.error(BizCodeEnume.PORDUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PORDUCT_UP_EXCEPTION.getMsg());
        }
    }
}
