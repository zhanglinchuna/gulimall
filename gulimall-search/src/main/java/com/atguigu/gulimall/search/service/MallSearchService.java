package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

public interface MallSearchService {

    /**
     *
     * @param searchParam   检索条件
     * @return
     */
    SearchResult search(SearchParam searchParam);
}
