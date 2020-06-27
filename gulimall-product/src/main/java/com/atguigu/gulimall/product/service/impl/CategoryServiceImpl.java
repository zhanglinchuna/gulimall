package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redisson;

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

    /**
     * 修改 菜单分类
     *
     * @param categoryEntity
     */

    /*@Caching(evict = {
            @CacheEvict(value = {"category"}, key = "'getLevel1Categorys'"),
            @CacheEvict(value = {"category"}, key = "'getCatelogJsonFormDb'")
    })  // 同时删除 一级分类 和 三级分类 的缓存*/
    @CacheEvict(value = {"category"}, allEntries = true)    // 删除 category 分区下所有缓存
    @Transactional
    @Override
    public void updateDetail(CategoryEntity categoryEntity) {
        this.updateById(categoryEntity);
        if (StringUtils.isNotEmpty(categoryEntity.getName())) {
            relationService.updateByCategoryDetail(categoryEntity.getCatId(), categoryEntity.getName());
        }
    }

    public List<Long> findParentPath(Long catelogId, List<Long> catelogIds) {
        catelogIds.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if (categoryEntity.getParentCid() != 0) {
            findParentPath(categoryEntity.getParentCid(), catelogIds);
        }
        return catelogIds;
    }

    @Cacheable(value = {"category"},key = "#root.method.name",sync = true)  // 将查询到的 一级分类 放入缓存中
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0L));
    }

    public Map<Long, List<Catelog2Vo>> getCatelogJsonLock() {

        /**
         *  缓存三大问题：
         *  1、缓存穿透：重复查询一个不存在key的数据，缓存中获取不到，总是查询数据库
         *      解决办法：将数据库中查询到的null值结果，存放到缓存中(key -> null)，设置较短的失效时间
         *
         *  2、缓存雪崩：缓存中所有数据都在同一时刻失效，大量查询请求发送给数据库，导致数据库服务崩溃
         *      解决办法：设置缓存失效时间时，加个随机值
         *
         *  3、缓存击穿：缓存中一个热点数据失效，大量查询请求发送给数据库，导致数据库服务崩溃
         *      解决办法：加锁，当第一次查询从缓存中获取不到数据时，在去查询数据库，查询数据的方法上
         *      加锁，当第一次的查询结果查到数据后放入到缓存中，后面的查询在去缓存中获取数据
         */

        // 1. 先从缓存中获取数据
        String catelogJson = redisTemplate.opsForValue().get("catelogJson");
        // 2. 如果缓存没有数据，再去查数据库
        if (StringUtils.isEmpty(catelogJson)) {
            Map<Long, List<Catelog2Vo>> catelog = getCatelogJsonFormDbWithRedissonLock();
            return catelog;
        }

        // JSON.parseObject(String text, TypeReference<T> type) 将json字符串转成指定复杂类型对象
        return JSON.parseObject(catelogJson, new TypeReference<Map<Long, List<Catelog2Vo>>>() {
        });
    }

    @Cacheable(value = {"category"},key = "#root.methodName",sync = true)
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

    /**
     * 本地锁
     *
     * @return
     */
    public Map<Long, List<Catelog2Vo>> getCatelogJsonFormDbWithLocalLock() {
        // springboot中所有组件都是单实例的
        // TODO 使用本地锁；分布式情况下无法锁住所有请求
        synchronized (this) {
            return getCatelogJsonFormDb();
        }
    }

    public Map<Long, List<Catelog2Vo>> getCatelogJsonFormDbWithRedissonLock() {

        RLock lock = redisson.getLock("catelogJson-lock");
        lock.lock();
        Map<Long, List<Catelog2Vo>> map;
        try {
            map = getCatelogJsonFormDb();
        } finally {
            lock.unlock();
        }
        return map;
    }
    /**
     * 分布式锁
     *
     * @return
     */
    public Map<Long, List<Catelog2Vo>> getCatelogJsonFormDbWithRedisLock() {

        String uuid = UUID.randomUUID().toString();
        // 分布式锁的原理：利用redis的 set key value EX NX 命令只允许一个客户端设置相同的key值（EX:设置键key的过期时间,NX:只有键key不存在的时候才会设置key的值）
        // 需要设置key的失效时间，否则可能系统故障无法删除key值为lock数据，导致死锁的问题发生
        Boolean bool = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (bool) {
            // 成功获取锁
            Map<Long, List<Catelog2Vo>> map;
            try {
                map = getCatelogJsonFormDb();
            } finally {
                // 释放锁
                /*if (redisTemplate.opsForValue().get("lock").equals(uuid)) {
                    // 删除自己的锁
                    redisTemplate.delete("lock");
                }*/
                // 比对自己锁的值是否相等，相等则为自己的锁，比对锁的值 和 删除锁 必须为原子操作
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                redisTemplate.execute(new DefaultRedisScript<Long>(script, long.class), Arrays.asList("lock"), uuid);
            }
            return map;
        }else {
            // 获取锁失败，重新回调方法进行重试
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatelogJsonFormDbWithRedisLock();
        }
    }

    private Map<Long, List<Catelog2Vo>> getCatelogJsonFormDb() {

        // 查数据库之前，在去从缓存中获取一遍数据，缓存中有数据就直接返回，没有数据就在查数据库
        String catelogJson = redisTemplate.opsForValue().get("catelogJson");
        if (!StringUtils.isEmpty(catelogJson)) {
            return JSON.parseObject(catelogJson, new TypeReference<Map<Long, List<Catelog2Vo>>>() {
            });
        }

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
        // 3. 将查询到的数据放入缓存中
        redisTemplate.opsForValue().set("catelogJson", JSON.toJSONString(collect), 1, TimeUnit.DAYS);
        return collect;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> categoryEntityList, Long catId) {
        return categoryEntityList.stream().filter(item -> item.getParentCid() == catId).collect(Collectors.toList());
    }
}