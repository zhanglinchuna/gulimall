package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author zhanglinchun
 * @email 2601165134@qq.com
 * @date 2020-04-05 18:02:18
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
