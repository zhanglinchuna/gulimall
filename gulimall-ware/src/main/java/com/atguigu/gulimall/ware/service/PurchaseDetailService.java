package com.atguigu.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;

import java.util.Map;

/**
 * 
 *
 * @author zhanglinchun
 * @email 2601165134@qq.com
 * @date 2020-04-05 18:06:50
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

