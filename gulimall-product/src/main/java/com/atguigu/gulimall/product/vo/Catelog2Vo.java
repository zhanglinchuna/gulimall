package com.atguigu.gulimall.product.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 二级分类VO
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catelog2Vo {

    /**
     * 一级父分类id
     */
    private Long catalog1Id;
    /**
     * 三级分类
     */
    private List<Catelog3Vo> catalog3List;
    /**
     * 二级分类id
     */
    private Long id;
    /**
     * 二级分类名字
     */
    private String name;

    /**
     * 三级分类VO
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catelog3Vo{
        /**
         * 二级父分类id
         */
        private Long catalog2Id;
        /**
         * 三级分类id
         */
        private Long id;
        /**
         * 三级分类名字
         */
        private String name;
    }
}
