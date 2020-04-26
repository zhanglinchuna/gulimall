package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.AttrGroupRelationVO;
import com.atguigu.gulimall.product.vo.AttrRespVO;
import com.atguigu.gulimall.product.vo.AttrVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author zhanglinchun
 * @email 2601165134@qq.com
 * @date 2020-04-05 17:57:00
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(AttrVO attr);

    PageUtils queryAttrBasePage(Map<String, Object> params, Long catelogId, String attrType);

    AttrRespVO getByInfo(Long attrId);

    void updateByInfo(AttrRespVO respVO);

    List<AttrEntity> getAttrRelation(Long attrgroupId);

    void deleteAttrRelation(AttrGroupRelationVO[] vos);
}

