package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.ElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Qualifier("esRestClient")
    @Autowired
    private RestHighLevelClient client;

    @Override
    public boolean productStatUp(List<SkuEsModel> skuEsModels) throws IOException {

        // 构建 es 批量保存商品数据对象
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX); // 设置索引
            indexRequest.id(skuEsModel.getSkuId().toString()); // 设置id
            indexRequest.source(JSON.toJSONString(skuEsModel), XContentType.JSON); // 设置需要保存的数据，以json格式保存

            bulkRequest.add(indexRequest);
        }
        // 调用es客户端，批量保存商品检索数据
        BulkResponse bulk = client.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);

        // TODO 商品上架出错
        boolean b = bulk.hasFailures();
        if (!b) {
            List<String> collect = Arrays.stream(bulk.getItems()).map(item -> item.getId()).collect(Collectors.toList());
            log.info("商品上架成功:{}",collect);
        }
        return b;
    }
}
