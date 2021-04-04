package com.atguigu.gulimall.search.vo;

import java.util.List;

public class SearchParam {

    // 页面传递过来的全文查询匹配关键字
    private String keyword;
    // 三级分类id
    private Long catalog3Id;
    /**
     * 排序条件
     * sort = saleCount_asc/desc    按销量排序
     * sort = skuPrice_asc/desc     按价格排序
     * sort = hotScore_asc/desc     按热度评分排序（综合排序）
     */
    private String sort;

    private Integer hasStock;// 是否只显示有货 hasStock=0/1
    private String skuPrice;//价格区间查询  1_500/_500/500_
    private List<Long> brandId;//按照品牌进行查询，可多选   brandId=1,2,3
    private List<String> attrs;//按照商品属性进行筛选 attrs=2_5存:6寸
    private Integer pageNum;//分页页数

}
