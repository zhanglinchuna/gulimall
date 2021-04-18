package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.vo.AttrGroupWithattrVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        if (catelogId == 0 && StringUtils.isEmpty((String) params.get("key"))) {
            return queryPage(params);
        } else {
            String key = (String) params.get("key");
            QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId);
            if (!StringUtils.isEmpty(key)) {
                wrapper.and(obj -> {
                    obj.eq("attr_group_id", key).or().like("attr_group_name", key);
                });
            }
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithattrVo> getAttrGroupWithattrsByCatelogId(Long catelogId) {
        // 获取属性分组信息
        List<AttrGroupEntity> attrGroupEntityList = this.baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupWithattrVo> collect = attrGroupEntityList.stream().map(item -> {
            AttrGroupWithattrVo vo = new AttrGroupWithattrVo();
            BeanUtils.copyProperties(item, vo);
            // 获取当前分组关联的所有属性详情
            Long attrGroupId = item.getAttrGroupId();
            List<AttrEntity> attrs = attrService.getAttrRelation(attrGroupId);
            vo.setAttrs(attrs);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithattrsBySpuId(Long spuId, Long catalogId) {
        // 1.查询出当前SPU对应的所有属性的分组信息，以及当前分组下的所有属性对应的值
        return this.baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
    }
}