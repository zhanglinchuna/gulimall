package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构

        //2.1）、找到所有的一级分类
        List<CategoryEntity> level1Menus =
                entities.stream().filter(category -> category.getParentCid() == 0).
                        map((menu) -> {
                            menu.setChildren(getChildrens(menu, entities));
                            return menu;
                        }).
                        sorted((m1, m2) -> {
                            return (m1.getSort() == null ? 0 : m1.getSort()) - (m2.getSort() == null ? 0 : m2.getSort());
                        }).
                        collect(Collectors.toList());
        return level1Menus;
    }

    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(category -> category.getParentCid() == root.getCatId()).
                map((menu) -> {
                    //1、找到子菜单
                    menu.setChildren(getChildrens(menu, all));
                    return menu;
                }).
                sorted((m1, m2) -> {
                    //2、菜单的排序
                    return (m1.getSort() == null ? 0 : m1.getSort()) - (m2.getSort() == null ? 0 : m2.getSort());
                }).collect(Collectors.toList());
        return children;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检测当前删除的菜单在别的地方是否有引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> catelogIds = new ArrayList<>();
        List<Long> parentPath = this.findParentPath(catelogId, catelogIds);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @Transactional
    @Override
    public void updateDetail(CategoryEntity categoryEntity) {
        this.updateById(categoryEntity);
        if (StringUtils.isNotEmpty(categoryEntity.getName())){
            relationService.updateByCategoryDetail(categoryEntity.getCatId(),categoryEntity.getName());
        }
    }

    public List<Long> findParentPath(Long catelogId, List<Long> catelogIds) {
        catelogIds.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if (categoryEntity.getParentCid() != 0) {
            findParentPath(categoryEntity.getParentCid(),catelogIds);
        }
        return catelogIds;
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0L));
    }

    @Override
    public Map<Long, List<Catelog2Vo>> getCatelogJson() {

        List<CategoryEntity> categoryEntityList = this.baseMapper.selectList(null);

        // 1. 先查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(categoryEntityList, 0L);

        Map<Long, List<Catelog2Vo>> collect = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId(), v -> {
            // 查找所有二级分类
            List<CategoryEntity> category2Entities = getParent_cid(categoryEntityList, v.getCatId());
            List<Catelog2Vo> catelog2 = null;
            if (null != category2Entities) {
                catelog2 = category2Entities.stream().map(item -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId(), null, item.getCatId(), item.getName());
                    List<CategoryEntity> category3Entities = getParent_cid(categoryEntityList, item.getCatId());
                    List<Catelog2Vo.Catelog3Vo> catelog3 = null;
                    // 查找所有三级分类
                    if (null != category3Entities) {
                        catelog3 = category3Entities.stream().map(category -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(item.getCatId(), category.getCatId(), category.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                    }
                    catelog2Vo.setCatalog3List(catelog3);
                    return catelog2Vo;
                }).collect(Collectors.toList());

            }
            return catelog2;
        }));
        return collect;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> categoryEntityList, Long catId) {
        return categoryEntityList.stream().filter(item -> item.getParentCid() == catId).collect(Collectors.toList());
    }
}