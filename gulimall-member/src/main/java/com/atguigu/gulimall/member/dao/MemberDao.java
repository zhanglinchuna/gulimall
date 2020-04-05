package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zhanglinchun
 * @email 2601165134@qq.com
 * @date 2020-04-05 18:00:12
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
